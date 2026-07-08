package fieldmind.research.app.features.field.data.question

import fieldmind.research.app.features.field.data.analysis.DetectedPattern
import fieldmind.research.app.features.field.data.database.entity.EvidenceAttachmentEntity
import fieldmind.research.app.features.field.data.database.entity.ObservationEntity
import fieldmind.research.app.features.field.data.database.entity.ProjectEntity
import fieldmind.research.app.features.field.data.database.entity.QuestionEntity
import fieldmind.research.app.features.field.data.database.entity.SourceEntity
import fieldmind.research.app.features.field.data.database.entity.SpeciesEntity

/**
 * A suggested question ready to be persisted.
 *
 * @property questionText The generated question string.
 * @property category Observation category this relates to.
 * @property sourceType Where this question came from (Observation, Pattern, Data gap, Species, Method, etc.).
 * @property priority Priority level: "High", "Medium", or "Low".
 * @property context Supporting context explaining why this question was generated.
 * @property specificityScore 0.0–1.0 how specific/concrete this question is (higher = more specific).
 * @property observationId Linked observation if applicable.
 * @property projectId Linked project if applicable.
 * @property speciesId Linked species if applicable.
 * @property relatedCategories Categories this question touches.
 * @property hypothesisHint A testable prediction that could be derived from this question.
 */
data class GeneratedQuestion(
    val questionText: String,
    val category: String,
    val sourceType: String,
    val priority: String,
    val context: String,
    val specificityScore: Float = 0.5f,
    val observationId: Long? = null,
    val projectId: Long? = null,
    val speciesId: Long? = null,
    val relatedCategories: List<String> = emptyList(),
    val hypothesisHint: String = ""
)

/**
 * Enhanced offline question generator that creates structured, contextual questions
 * from observations, detected patterns, species data, evidence gaps, and more.
 *
 * Improvements over the original:
 * 1. **Pattern-aware** — Uses [DetectedPattern] results for real pattern-based questions
 * 2. **Species-aware** — Uses [SpeciesEntity] (scientific names, conservation, taxonomy)
 * 3. **Evidence gap** — Analyzes observations with/without attachments for evidence questions
 * 4. **Temporal depth** — Time-of-day and date-span analysis for specific timing questions
 * 5. **Confidence-weighted** — Prefers low-confidence observations for uncertainty questions
 * 6. **Category-specific templates** — Different question types per observation category
 * 7. **Hypothesis-ready** — Includes testable prediction hints for hypothesis creation
 * 8. **Multi-modal** — Questions about attached media, audio, evidence
 * 9. **Cross-reference** — Questions spanning projects, linking species to observations
 * 10. **Specificity scoring** — Ranks questions by concreteness, prefers high-specificity
 *
 * Question types:
 * - **Observation** — direct questions from what was observed
 * - **Comparison** — compare across categories, locations, or times
 * - **Pattern** — questions about detected patterns or changes
 * - **Cause** — questions about why something was observed
 * - **Gap** — questions about categories/locations not yet explored
 * - **Evidence gap** — questions about missing evidence
 * - **Prediction** — questions predicting future observations
 * - **Species** — questions derived from species catalog data
 * - **Method** — questions about how to observe more effectively
 * - **Multi-modal** — questions about photos, audio, or evidence
 */
object QuestionGenerator {

    private const val MAX_QUESTIONS = 20

    /**
     * Generate questions from all available data sources.
     *
     * @param observations All user observations.
     * @param species Registered species with taxonomy and conservation data.
     * @param projects Research projects.
     * @param sources Reference sources.
     * @param evidenceAttachments Media evidence attached to observations.
     * @param patterns Detected patterns from [PatternDetectionEngine].
     * @param existing Existing questions for deduplication.
     */
    fun generateAll(
        observations: List<ObservationEntity>,
        species: List<SpeciesEntity> = emptyList(),
        projects: List<ProjectEntity> = emptyList(),
        sources: List<SourceEntity> = emptyList(),
        evidenceAttachments: List<EvidenceAttachmentEntity> = emptyList(),
        patterns: List<DetectedPattern> = emptyList(),
        existing: List<QuestionEntity> = emptyList()
    ): List<GeneratedQuestion> {
        if (observations.isEmpty()) return emptyList()

        val existingTexts = existing.map { it.questionText.lowercase().trim() }.toSet()
        val results = mutableListOf<GeneratedQuestion>()

        // 1. Pattern-aware questions from detected patterns
        results.addAll(generateFromPatterns(patterns, existingTexts))

        // 2. Species-aware questions
        results.addAll(generateSpeciesQuestions(observations, species, existingTexts))

        // 3. Cross-reference questions (observations ↔ projects)
        results.addAll(generateCrossReferenceQuestions(observations, projects, existingTexts))

        // 4. Evidence gap questions
        results.addAll(generateEvidenceGapQuestions(observations, evidenceAttachments, existingTexts))

        // 5. Multi-modal questions (about attached media)
        results.addAll(generateMultiModalQuestions(observations, evidenceAttachments, existingTexts))

        // 6. Confidence-weighted uncertainty questions
        results.addAll(generateConfidenceQuestions(observations, existingTexts))

        // 7. Temporal depth questions
        results.addAll(generateTemporalQuestions(observations, existingTexts))

        // 8. Category-specific questions
        results.addAll(generateCategorySpecificQuestions(observations, existingTexts))

        // 9. Classic observation questions (improved templates)
        results.addAll(generateObservationQuestions(observations, existingTexts))

        // 10. Comparison questions
        results.addAll(generateComparisonQuestions(observations, existingTexts))

        // 11. Cause questions
        results.addAll(generateCauseQuestions(observations, existingTexts))

        // 12. Gap questions
        results.addAll(generateGapQuestions(observations, existingTexts))

        // 13. Prediction questions
        results.addAll(generatePredictionQuestions(observations, existingTexts))

        // 14. Method questions
        results.addAll(generateMethodQuestions(observations, existingTexts))

        // Sort by specificity score descending (most specific first), take top MAX
        return results
            .distinctBy { it.questionText.lowercase().trim() }
            .sortedByDescending { it.specificityScore }
            .take(MAX_QUESTIONS)
    }

    // ══════════════════════════════════════════════════════════════════
    //  1. Pattern-aware questions (from PatternDetectionEngine results)
    // ══════════════════════════════════════════════════════════════════

    private fun generateFromPatterns(
        patterns: List<DetectedPattern>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        if (patterns.isEmpty()) return emptyList()
        val results = mutableListOf<GeneratedQuestion>()

        patterns.forEach { pattern ->
            when (pattern.type) {
                "repeated_subject" -> {
                    pattern.relatedSubjects.firstOrNull()?.let { subject ->
                        val q = "What environmental or seasonal factors drive the repeated observations of \"$subject\" (${pattern.count} times)?"
                        if (q.lowercase() !in existing) {
                            results.add(GeneratedQuestion(
                                questionText = q,
                                category = pattern.relatedCategories.firstOrNull() ?: "General",
                                sourceType = "Pattern",
                                priority = "High",
                                specificityScore = 0.85f,
                                context = pattern.insight.ifBlank { "\"$subject\" has been observed ${pattern.count} times across ${pattern.relatedCategories.distinct().size} categories." },
                                relatedCategories = pattern.relatedCategories
                            ))
                        }
                    }
                }
                "site_revisit" -> {
                    pattern.relatedLocations.firstOrNull()?.let { loc ->
                        val q = "What changes have occurred at \"$loc\" across ${pattern.count} visits?"
                        if (q.lowercase() !in existing) {
                            results.add(GeneratedQuestion(
                                questionText = q,
                                category = "Site",
                                sourceType = "Pattern",
                                priority = "High",
                                specificityScore = 0.9f,
                                context = pattern.insight.ifBlank { "\"$loc\" has been visited ${pattern.count} times. Tracking changes over time could reveal seasonal trends." },
                                relatedCategories = pattern.relatedCategories
                            ))
                        }
                    }
                }
                "temporal_cluster" -> {
                    val q = "Why are observations concentrated during ${pattern.label.removePrefix("Peak observation time: ")} — is this when target species are most active or when you are most available?"
                    if (q.lowercase() !in existing) {
                        results.add(GeneratedQuestion(
                            questionText = q,
                            category = "Timing",
                            sourceType = "Pattern",
                            priority = "Medium",
                            specificityScore = 0.8f,
                            context = pattern.insight.ifBlank { pattern.description },
                            relatedCategories = pattern.relatedCategories
                        ))
                    }
                }
                "weather_correlation" -> {
                    val q = "How does ${pattern.label.removePrefix("Mostly ")} weather affect which species or phenomena are observable?"
                    if (q.lowercase() !in existing) {
                        results.add(GeneratedQuestion(
                            questionText = q,
                            category = "Weather",
                            sourceType = "Pattern",
                            priority = "Medium",
                            specificityScore = 0.75f,
                            context = pattern.insight.ifBlank { pattern.description },
                            relatedCategories = pattern.relatedCategories
                        ))
                    }
                }
                "observation_gap" -> {
                    val q = "Why has \"${pattern.relatedCategories.firstOrNull() ?: "this category"}\" not been observed recently — is the subject seasonal, or should effort be redirected?"
                    if (q.lowercase() !in existing) {
                        results.add(GeneratedQuestion(
                            questionText = q,
                            category = pattern.relatedCategories.firstOrNull() ?: "General",
                            sourceType = "Data gap",
                            priority = "Medium",
                            specificityScore = 0.7f,
                            context = pattern.insight.ifBlank { pattern.description },
                            relatedCategories = pattern.relatedCategories
                        ))
                    }
                }
                "category_trend" -> {
                    if (pattern.label.startsWith("Underexplored")) {
                        val q = "What is hindering observations in \"${pattern.relatedCategories.firstOrNull() ?: "this category"}\" — access, season, or interest?"
                        if (q.lowercase() !in existing) {
                            results.add(GeneratedQuestion(
                                questionText = q,
                                category = pattern.relatedCategories.firstOrNull() ?: "General",
                                sourceType = "Data gap",
                                priority = "Low",
                                specificityScore = 0.65f,
                                context = pattern.insight.ifBlank { pattern.description },
                                relatedCategories = pattern.relatedCategories
                            ))
                        }
                    }
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  2. Species-aware questions (from SpeciesEntity + observations)
    // ══════════════════════════════════════════════════════════════════

    private fun generateSpeciesQuestions(
        observations: List<ObservationEntity>,
        species: List<SpeciesEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        if (species.isEmpty()) return emptyList()
        val results = mutableListOf<GeneratedQuestion>()

        // Group species by conservation status
        val conservationGroups = species.groupBy { it.conservationStatus }
        conservationGroups.forEach { (status, spList) ->
            if (status in listOf("Endangered", "Critically Endangered", "Vulnerable", "Near Threatened")) {
                spList.take(3).forEach { sp ->
                    // Find matching observations for this species
                    val matchingObs = observations.filter { obs ->
                        obs.subject.contains(sp.commonName, ignoreCase = true) ||
                        (sp.scientificName.isNotBlank() && obs.subject.contains(sp.scientificName.split(" ").first(), ignoreCase = true))
                    }
                    val sciName = if (sp.scientificName.isNotBlank()) " (${sp.scientificName})" else ""
                    val q = "What factors are contributing to the ${status.lowercase()} status of ${sp.commonName}$sciName at observed sites?"
                    if (q.lowercase() !in existing) {
                        results.add(GeneratedQuestion(
                            questionText = q,
                            category = "Conservation",
                            sourceType = "Species",
                            priority = "High",
                            specificityScore = 0.92f,
                            context = "${sp.commonName} is listed as $status${if (matchingObs.isNotEmpty()) " and has ${matchingObs.size} matching observations" else " but has no matching observations yet"}.",
                            speciesId = sp.id,
                            observationId = matchingObs.firstOrNull()?.id
                        ))
                    }
                }
            }
        }

        // Taxonomic diversity questions
        val families = species.filter { it.family.isNotBlank() }.groupBy { it.family }
        if (families.size >= 3) {
            val topFamily = families.maxByOrNull { it.value.size }
            topFamily?.let { (family, members) ->
                val q = "What explains the diversity of ${members.size} species in the $family family — is this a genuine hotspot or observation bias?"
                if (q.lowercase() !in existing) {
                    results.add(GeneratedQuestion(
                        questionText = q,
                        category = "Taxonomy",
                        sourceType = "Species",
                        priority = "Medium",
                        specificityScore = 0.78f,
                        context = "$family has ${members.size} species: ${members.map { it.commonName }.joinToString(", ")}."
                    ))
                }
            }
        }

        // Species with no observations (in catalog but never seen)
        val speciesWithObs = species.filter { it.observationCount > 0 }
        val speciesWithoutObs = species.filter { it.observationCount == 0 && it.commonName.isNotBlank() }
        if (speciesWithoutObs.isNotEmpty()) {
            speciesWithoutObs.take(2).forEach { sp ->
                val sciName = if (sp.scientificName.isNotBlank()) " (${sp.scientificName})" else ""
                val q = "Why has ${sp.commonName}$sciName not been observed yet — is it seasonal, rare, or in an unvisited location?"
                if (q.lowercase() !in existing) {
                    results.add(GeneratedQuestion(
                        questionText = q,
                        category = "Survey",
                        sourceType = "Species",
                        priority = "Medium",
                        specificityScore = 0.82f,
                        context = "${sp.commonName}$sciName is in the species catalog but has never been observed. ${if (sp.conservationStatus != "Not Evaluated") "Conservation status: ${sp.conservationStatus}." else ""}",
                        speciesId = sp.id
                    ))
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  3. Cross-reference questions (observations ↔ projects)
    // ══════════════════════════════════════════════════════════════════

    private fun generateCrossReferenceQuestions(
        observations: List<ObservationEntity>,
        projects: List<ProjectEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        if (projects.isEmpty()) return emptyList()
        val results = mutableListOf<GeneratedQuestion>()

        projects.filter { it.status == "Active" }.forEach { project ->
            val projectObs = observations.filter { it.projectId == project.id }
            if (projectObs.isEmpty()) {
                val q = "How can observations be directed to support the project \"${project.title}\"?"
                if (q.lowercase() !in existing) {
                    results.add(GeneratedQuestion(
                        questionText = q,
                        category = "General",
                        sourceType = "Cross-reference",
                        priority = "Low",
                        specificityScore = 0.6f,
                        context = "The project \"${project.title}\" has no linked observations yet. ${if (project.objective.isNotBlank()) "Objective: ${project.objective.take(200)}" else ""}",
                        projectId = project.id
                    ))
                }
            } else if (projectObs.size >= 3) {
                val categories = projectObs.map { it.category }.distinct().filterNot { it.isBlank() }
                val q = "What trends emerge from the ${projectObs.size} observations in \"${project.title}\" across ${categories.size} categories?"
                if (q.lowercase() !in existing) {
                    results.add(GeneratedQuestion(
                        questionText = q,
                        category = categories.firstOrNull() ?: "General",
                        sourceType = "Cross-reference",
                        priority = "Medium",
                        specificityScore = 0.75f,
                        context = "Project \"${project.title}\" has ${projectObs.size} observations in ${categories.joinToString(", ")}.",
                        projectId = project.id
                    ))
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  4. Evidence gap questions (observations without attachments)
    // ══════════════════════════════════════════════════════════════════

    private fun generateEvidenceGapQuestions(
        observations: List<ObservationEntity>,
        attachments: List<EvidenceAttachmentEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        if (observations.isEmpty()) return emptyList()
        val results = mutableListOf<GeneratedQuestion>()

        // Observations without evidence attachments but with potentially documentable subjects
        val obsWithAttachments = attachments.map { it.observationId }.toSet()
        val obsWithoutEvidence = observations
            .filter { it.id !in obsWithAttachments && it.subject.isNotBlank() }
            .sortedByDescending { it.timestamp }
            .take(3)

        obsWithoutEvidence.forEach { obs ->
            val q = "What photographic or audio evidence could strengthen the \"${obs.subject}\" observation from ${obs.date}?"
            if (q.lowercase() !in existing) {
                results.add(GeneratedQuestion(
                    questionText = q,
                    category = obs.category.ifBlank { "General" },
                    sourceType = "Evidence gap",
                    priority = "Medium",
                    specificityScore = 0.7f,
                    context = "Observation of \"${obs.subject}\" on ${obs.date} has no attached evidence. ${obs.factsOnlyNotes.take(100)}",
                    observationId = obs.id,
                    relatedCategories = listOf(obs.category)
                ))
            }
        }

        // Low-confidence observations worth re-verifying
        val lowConfidence = observations
            .filter { it.confidenceLevel.equals("Not sure", ignoreCase = true) || it.confidenceLevel.equals("Low", ignoreCase = true) }
            .sortedByDescending { it.timestamp }
            .take(2)

        lowConfidence.forEach { obs ->
            val q = "Can the low-confidence observation of \"${obs.subject}\" from ${obs.date} be verified with additional evidence or expert consultation?"
            if (q.lowercase() !in existing) {
                results.add(GeneratedQuestion(
                    questionText = q,
                    category = obs.category.ifBlank { "General" },
                    sourceType = "Evidence gap",
                    priority = "High",
                    specificityScore = 0.88f,
                    context = "Confidence was marked as \"${obs.confidenceLevel}\" for \"${obs.subject}\". ${obs.factsOnlyNotes.take(100)}",
                    observationId = obs.id,
                    relatedCategories = listOf(obs.category)
                ))
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  5. Multi-modal questions (about attached media)
    // ══════════════════════════════════════════════════════════════════

    private fun generateMultiModalQuestions(
        observations: List<ObservationEntity>,
        attachments: List<EvidenceAttachmentEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        if (attachments.isEmpty()) return emptyList()
        val results = mutableListOf<GeneratedQuestion>()

        // Group attachments by type
        val photoAttachments = attachments.filter { it.type.startsWith("image") || it.type == "Photo" }
        val audioAttachments = attachments.filter { it.type.startsWith("audio") || it.type == "Audio" }

        // Observations with photos but no caption
        val photosWithoutCaption = photoAttachments.filter { it.caption.isBlank() }
        if (photosWithoutCaption.isNotEmpty()) {
            photosWithoutCaption.take(2).forEach { att ->
                val obs = observations.firstOrNull { it.id == att.observationId }
                val subject = obs?.subject ?: "this observation"
                val q = "What specific details are visible in the photo attached to \"$subject\" that were not captured in the notes?"
                if (q.lowercase() !in existing) {
                    results.add(GeneratedQuestion(
                        questionText = q,
                        category = obs?.category ?: "General",
                        sourceType = "Multi-modal",
                        priority = "Medium",
                        specificityScore = 0.8f,
                        context = "Photo attached to \"$subject\" has no caption. Adding a detailed caption could reveal overlooked details.",
                        observationId = att.observationId,
                        relatedCategories = obs?.let { listOf(it.category) } ?: emptyList()
                    ))
                }
            }
        }

        // Observations with audio recordings
        if (audioAttachments.isNotEmpty()) {
            audioAttachments.take(1).forEach { att ->
                val obs = observations.firstOrNull { it.id == att.observationId }
                val subject = obs?.subject ?: "this observation"
                val q = "What animal calls or environmental sounds can be identified in the audio recording for \"$subject\"?"
                if (q.lowercase() !in existing) {
                    results.add(GeneratedQuestion(
                        questionText = q,
                        category = obs?.category ?: "General",
                        sourceType = "Multi-modal",
                        priority = "Low",
                        specificityScore = 0.65f,
                        context = "Audio recording attached to \"$subject\". Bird calls, insect stridulation, or environmental sounds may be identifiable.",
                        observationId = att.observationId,
                        relatedCategories = obs?.let { listOf(it.category) } ?: emptyList()
                    ))
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  6. Confidence-weighted uncertainty questions
    // ══════════════════════════════════════════════════════════════════

    private fun generateConfidenceQuestions(
        observations: List<ObservationEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        val results = mutableListOf<GeneratedQuestion>()

        // Observations where confidence is inconsistent
        val confidenceLevels = observations
            .filter { it.confidenceLevel.isNotBlank() }
            .map { it.confidenceLevel }
            .distinct()

        if (confidenceLevels.size >= 3) {
            // Check if the same subject has multiple confidence levels
            val inconsistentSubjects = observations
                .filter { it.subject.isNotBlank() }
                .groupBy { it.subject.trim().lowercase() }
                .filter { (_, obsList) -> obsList.map { it.confidenceLevel }.distinct().size >= 2 }
                .entries
                .take(2)

            inconsistentSubjects.forEach { (_, obsList) ->
                val displayName = obsList.first().subject
                val levels = obsList.map { it.confidenceLevel }.distinct()
                val q = "Why does the confidence level for \"$displayName\" vary between \"${levels.joinToString("\" and \"")}\" across observations?"
                if (q.lowercase() !in existing) {
                    results.add(GeneratedQuestion(
                        questionText = q,
                        category = obsList.first().category.ifBlank { "General" },
                        sourceType = "Uncertainty",
                        priority = "Medium",
                        specificityScore = 0.83f,
                        context = "\"$displayName\" has ${obsList.size} observations with inconsistent confidence: ${levels.joinToString(", ")}.",
                        observationId = obsList.first().id,
                        relatedCategories = obsList.map { it.category }.distinct()
                    ))
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  7. Temporal depth questions (time spans, date gaps)
    // ══════════════════════════════════════════════════════════════════

    private fun generateTemporalQuestions(
        observations: List<ObservationEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        val results = mutableListOf<GeneratedQuestion>()

        // Find subjects observed across the longest time span
        val subjectTimeSpans = observations
            .filter { it.subject.isNotBlank() }
            .groupBy { it.subject.trim().lowercase() }
            .mapValues { (_, obsList) ->
                val timestamps = obsList.map { it.timestamp }
                (timestamps.maxOrNull() ?: 0L) - (timestamps.minOrNull() ?: 0L)
            }
            .filter { it.value > 0 }
            .entries
            .sortedByDescending { it.value }
            .take(2)

        subjectTimeSpans.forEach { (_, spanMs) ->
            val spanDays = (spanMs / 86_400_000L).toInt()
            if (spanDays >= 7) {
                val subjectKey = observations.filter { it.subject.isNotBlank() }
                    .groupBy { it.subject.trim().lowercase() }
                    .entries.firstOrNull { e -> e.value.any { obs -> obs.timestamp.let { t -> (observations.maxOf { o -> o.timestamp } - observations.minOf { o -> o.timestamp }) == spanMs } } }
                subjectKey?.let { (_, obsList) ->
                    val displayName = obsList.first().subject
                    val count = obsList.size
                    val q = "How has \"$displayName\" changed over the $spanDays-day span across $count observations?"
                    if (q.lowercase() !in existing) {
                        results.add(GeneratedQuestion(
                            questionText = q,
                            category = obsList.first().category.ifBlank { "General" },
                            sourceType = "Temporal",
                            priority = "High",
                            specificityScore = 0.87f,
                            context = "\"$displayName\" observed $count times over $spanDays days. This span allows analysis of behavioral or population changes.",
                            observationId = obsList.first().id,
                            relatedCategories = obsList.map { it.category }.distinct()
                        ))
                    }
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  8. Category-specific questions
    // ══════════════════════════════════════════════════════════════════

    private fun generateCategorySpecificQuestions(
        observations: List<ObservationEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        val results = mutableListOf<GeneratedQuestion>()

        // Group observations by category for targeted questions
        val byCategory = observations.filter { it.category.isNotBlank() && it.subject.isNotBlank() }
            .groupBy { it.category }

        byCategory.forEach { (category, obsList) ->
            if (obsList.size < 2) return@forEach

            when (category.lowercase()) {
                "bird", "birds" -> {
                    // Bird-specific: migration, plumage, behavior
                    val topBird = obsList.groupBy { it.subject.trim().lowercase() }.maxByOrNull { it.value.size }
                    topBird?.let { (_, list) ->
                        val name = list.first().subject
                        val q = "What plumage variations or behavioral patterns are observed in \"$name\" across different seasons?"
                        if (q.lowercase() !in existing) {
                            results.add(GeneratedQuestion(
                                questionText = q,
                                category = category,
                                sourceType = "Category-specific",
                                priority = "High",
                                specificityScore = 0.85f,
                                context = "\"$name\" observed ${list.size} times. Tracking seasonal changes in appearance and behavior could reveal migration or breeding patterns.",
                                observationId = list.first().id,
                                relatedCategories = listOf(category)
                            ))
                        }
                    }
                }
                "mammal", "mammals" -> {
                    val topMammal = obsList.groupBy { it.subject.trim().lowercase() }.maxByOrNull { it.value.size }
                    topMammal?.let { (_, list) ->
                        val name = list.first().subject
                        val q = "What is the activity pattern of \"$name\" — is it diurnal, crepuscular, or nocturnal at this site?"
                        if (q.lowercase() !in existing) {
                            results.add(GeneratedQuestion(
                                questionText = q,
                                category = category,
                                sourceType = "Category-specific",
                                priority = "Medium",
                                specificityScore = 0.82f,
                                context = "\"$name\" observed ${list.size} times. Analyzing time-of-day patterns could reveal its activity cycle.",
                                observationId = list.first().id,
                                relatedCategories = listOf(category)
                            ))
                        }
                    }
                }
                "plant", "plants", "flower", "wildflower" -> {
                    val q = "What is the flowering or fruiting phenology of observed plants — are they blooming earlier or later than expected?"
                    if (q.lowercase() !in existing) {
                        results.add(GeneratedQuestion(
                            questionText = q,
                            category = category,
                            sourceType = "Category-specific",
                            priority = "Medium",
                            specificityScore = 0.72f,
                            context = "${obsList.size} observations in \"$category\". Tracking phenology over time can reveal climate change impacts.",
                            relatedCategories = listOf(category)
                        ))
                    }
                }
                "insect", "insects", "butterfly", "bee" -> {
                    val q = "Which plant species are most frequently visited by observed pollinators?"
                    if (q.lowercase() !in existing) {
                        results.add(GeneratedQuestion(
                            questionText = q,
                            category = category,
                            sourceType = "Category-specific",
                            priority = "Medium",
                            specificityScore = 0.78f,
                            context = "${obsList.size} pollinator observations. Identifying preferred host plants can guide habitat conservation.",
                            relatedCategories = listOf(category)
                        ))
                    }
                }
                "weather", "climate" -> {
                    val q = "How do local weather patterns compare with broader regional climate trends?"
                    if (q.lowercase() !in existing) {
                        results.add(GeneratedQuestion(
                            questionText = q,
                            category = category,
                            sourceType = "Category-specific",
                            priority = "Low",
                            specificityScore = 0.5f,
                            context = "${obsList.size} weather observations logged. Comparing with regional data could reveal microclimate patterns.",
                            relatedCategories = listOf(category)
                        ))
                    }
                }
                else -> {
                    // Generic category question for any category with 3+ observations
                    if (obsList.size >= 3) {
                        val q = "What distinguishes the ${obsList.size} observations in \"$category\" — are there recurring themes or outliers?"
                        if (q.lowercase() !in existing) {
                            results.add(GeneratedQuestion(
                                questionText = q,
                                category = category,
                                sourceType = "Category-specific",
                                priority = "Low",
                                specificityScore = 0.55f,
                                context = "${obsList.size} observations in \"$category\". Common patterns or outliers could reveal interesting research directions.",
                                relatedCategories = listOf(category)
                            ))
                        }
                    }
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  9. Observation questions — direct from what was seen (improved)
    // ══════════════════════════════════════════════════════════════════

    private fun generateObservationQuestions(
        observations: List<ObservationEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        return observations
            .filter { it.subject.isNotBlank() }
            .sortedByDescending { it.timestamp }
            .take(5)
            .mapNotNull { obs ->
                // Choose a more specific template based on available data
                val hasWeather = obs.weatherCondition.isNotBlank()
                val hasLocation = obs.manualLocation.isNotBlank() || (obs.latitude != null && obs.longitude != null)
                val hasEvidence = obs.evidenceSummary.isNotBlank()

                val q = when {
                    hasWeather && hasLocation -> "How does the weather condition \"${obs.weatherCondition}\" at \"${obs.manualLocation.ifBlank { "this location" }}\" influence the behavior of \"${obs.subject}\"?"
                    hasWeather -> "Does ${obs.weatherCondition.lowercase()} weather correlate with the presence or activity level of \"${obs.subject}\"?"
                    hasLocation -> "What environmental factors at \"${obs.manualLocation}\" make it a suitable habitat for \"${obs.subject}\"?"
                    hasEvidence -> "What does the evidence for \"${obs.subject}\" (${obs.evidenceSummary.take(80)}) suggest about its role in the local ecosystem?"
                    else -> "What specific ecological role does \"${obs.subject}\" play at the observed site?"
                }

                if (q.lowercase() in existing) return@mapNotNull null

                val specificity = when {
                    hasWeather && hasLocation -> 0.78f
                    hasWeather -> 0.72f
                    hasLocation -> 0.68f
                    hasEvidence -> 0.62f
                    else -> 0.55f
                }

                GeneratedQuestion(
                    questionText = q,
                    category = obs.category.ifBlank { "Other" },
                    sourceType = "Observation",
                    priority = "Medium",
                    specificityScore = specificity,
                    context = when {
                        hasWeather && hasLocation -> "${obs.subject} observed at ${obs.manualLocation} during ${obs.weatherCondition} on ${obs.date}."
                        hasWeather -> "${obs.subject} observed during ${obs.weatherCondition} on ${obs.date}."
                        hasLocation -> "${obs.subject} observed at ${obs.manualLocation} on ${obs.date}."
                        else -> "Based on observation of ${obs.subject} on ${obs.date}: ${obs.factsOnlyNotes.take(150)}"
                    },
                    observationId = obs.id,
                    relatedCategories = listOf(obs.category)
                )
            }
    }

    // ══════════════════════════════════════════════════════════════════
    //  10. Comparison questions — compare across categories, locations, or times
    // ══════════════════════════════════════════════════════════════════

    private fun generateComparisonQuestions(
        observations: List<ObservationEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        val results = mutableListOf<GeneratedQuestion>()

        // Compare two most-observed categories
        val categories = observations
            .filter { it.category.isNotBlank() }
            .groupBy { it.category }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .take(2)

        if (categories.size == 2) {
            val (c1, c2) = categories[0].key to categories[1].key
            val q = "How do the observation patterns differ between the \"$c1\" (${categories[0].value}) and \"$c2\" (${categories[1].value}) categories — are they driven by abundance, accessibility, or research focus?"
            if (q.lowercase() !in existing) {
                results.add(GeneratedQuestion(
                    questionText = q,
                    category = "Comparison",
                    sourceType = "Observation",
                    priority = "Medium",
                    specificityScore = 0.72f,
                    context = "${categories[0].value} observations in \"$c1\" vs ${categories[1].value} in \"$c2\". The imbalance may reflect real abundance or observation bias.",
                    relatedCategories = listOf(c1, c2)
                ))
            }
        }

        // Compare two locations if available
        val locations = observations
            .filter { it.manualLocation.isNotBlank() }
            .groupBy { it.manualLocation }
            .mapValues { it.value.size }
            .entries
            .filter { it.value >= 2 }
            .sortedByDescending { it.value }
            .take(2)

        if (locations.size == 2) {
            val (l1, l2) = locations[0].key to locations[1].key
            val count1 = locations[0].value
            val count2 = locations[1].value
            val q = "What habitat differences between \"$l1\" ($count1 observations) and \"$l2\" ($count2 observations) explain the distinct species observed at each?"
            if (q.lowercase() !in existing) {
                results.add(GeneratedQuestion(
                    questionText = q,
                    category = "Comparison",
                    sourceType = "Observation",
                    priority = "Low",
                    specificityScore = 0.74f,
                    context = "$count1 observations at \"$l1\" and $count2 at \"$l2\". Comparing species composition can reveal habitat preferences.",
                    relatedCategories = emptyList()
                ))
            }
        }

        return results
    }

    // ── 3. Cause questions — why was something observed ──

    private fun generateCauseQuestions(
        observations: List<ObservationEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        val results = mutableListOf<GeneratedQuestion>()

        // Look for weather-attached observations to ask about causality
        val withWeather = observations.filter { it.weatherCondition.isNotBlank() && it.subject.isNotBlank() }
        if (withWeather.isNotEmpty()) {
            val obs = withWeather.first()
            val q = "Does the weather condition \"${obs.weatherCondition}\" influence the presence or behavior of \"${obs.subject}\"?"
            if (q.lowercase() !in existing) {
                results.add(GeneratedQuestion(
                    questionText = q,
                    category = obs.category.ifBlank { "Other" },
                    sourceType = "Observation",
                    priority = "High",
                    context = "${obs.subject} was observed during ${obs.weatherCondition}. Weather data was automatically attached.",
                    observationId = obs.id,
                    relatedCategories = listOf(obs.category)
                ))
            }
        }

        // Time-based cause
        val timeObservations = observations.filter { it.time.isNotBlank() && it.subject.isNotBlank() }
        if (timeObservations.isNotEmpty()) {
            val obs = timeObservations.first()
            val hour = obs.time.split(":").firstOrNull()?.toIntOrNull()
            if (hour != null) {
                val timeDesc = when (hour) {
                    in 5..8 -> "early morning"
                    in 9..11 -> "late morning"
                    in 12..14 -> "midday"
                    in 15..17 -> "afternoon"
                    in 18..20 -> "evening"
                    else -> "night"
                }
                val q = "Why is \"${obs.subject}\" most commonly observed during $timeDesc?"
                if (q.lowercase() !in existing) {
                    results.add(GeneratedQuestion(
                        questionText = q,
                        category = obs.category.ifBlank { "Other" },
                        sourceType = "Observation",
                        priority = "Medium",
                        context = "Based on timing of observations for \"${obs.subject}\".",
                        observationId = obs.id,
                        relatedCategories = listOf(obs.category)
                    ))
                }
            }
        }

        return results
    }


    // ── 5. Gap questions — what haven't we explored ──

    private fun generateGapQuestions(
        observations: List<ObservationEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        val results = mutableListOf<GeneratedQuestion>()

        val categories = observations
            .filter { it.category.isNotBlank() }
            .groupBy { it.category }

        // Find underexplored categories
        val underexplored = categories.filter { it.value.size == 1 }.toList()
        for ((cat, obsList) in underexplored.take(2)) {
            val q = "What more can we learn about \"$cat\" — currently only ${obsList.size} observation?"
            if (q.lowercase() !in existing) {
                results.add(GeneratedQuestion(
                    questionText = q,
                    category = cat,
                    sourceType = "Data gap",
                    priority = "Low",
                    context = "Only ${obsList.size} observation in \"$cat\". Exploring more could reveal new insights.",
                    observationId = obsList.first().id,
                    relatedCategories = listOf(cat)
                ))
            }
        }

        // Investigate if a location was visited only once
        val singleVisitLocations = observations
            .filter { it.manualLocation.isNotBlank() }
            .groupBy { it.manualLocation }
            .filter { it.value.size == 1 }
            .entries.toList()
            .take(1)

        for ((loc, _) in singleVisitLocations) {
            val q = "Should \"$loc\" be revisited for follow-up observations?"
            if (q.lowercase() !in existing) {
                results.add(GeneratedQuestion(
                    questionText = q,
                    category = "General",
                    sourceType = "Data gap",
                    priority = "Low",
                    context = "\"$loc\" was visited only once. Revisiting could reveal seasonal or temporal differences."
                ))
            }
        }

        return results
    }

    // ── 6. Prediction questions — what might happen next ──

    private fun generatePredictionQuestions(
        observations: List<ObservationEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        val results = mutableListOf<GeneratedQuestion>()

        // Ask about a highly-observed subject
        val topSubject = observations
            .filter { it.subject.isNotBlank() }
            .groupBy { it.subject.trim().lowercase() }
            .maxByOrNull { it.value.size }

        if (topSubject != null && topSubject.value.size >= 2) {
            val displayName = topSubject.value.first().subject
            val q = "Will \"$displayName\" continue to be observed at the same frequency in the coming weeks?"
            if (q.lowercase() !in existing) {
                results.add(GeneratedQuestion(
                    questionText = q,
                    category = "Prediction",
                    sourceType = "Observation",
                    priority = "Medium",
                    context = "Based on ${topSubject.value.size} past observations of \"$displayName\".",
                    observationId = topSubject.value.first().id,
                    relatedCategories = topSubject.value.map { it.category }.distinct()
                ))
            }
        }

        // Weather-based prediction (if weather was attached)
        val withWeather = observations.filter { it.weatherCondition.isNotBlank() }
        if (withWeather.size >= 3) {
            val q = "How will changing weather conditions affect future observations?"
            if (q.lowercase() !in existing) {
                results.add(GeneratedQuestion(
                    questionText = q,
                    category = "Prediction",
                    sourceType = "Observation",
                    priority = "Low",
                    context = "Weather data has been logged for ${withWeather.size} observations."
                ))
            }
        }

        return results
    }

    // ── 7. Method questions — how to improve observations ──

    private fun generateMethodQuestions(
        observations: List<ObservationEntity>,
        existing: Set<String>
    ): List<GeneratedQuestion> {
        val results = mutableListOf<GeneratedQuestion>()

        // If attachments are mentioned, ask about documentation methods
        val withEvidence = observations.filter { it.evidenceSummary.isNotBlank() }
        if (withEvidence.isNotEmpty()) {
            val obs = withEvidence.first()
            val q = "What is the most effective way to document evidence for \"${obs.subject}\" observations?"
            if (q.lowercase() !in existing) {
                results.add(GeneratedQuestion(
                    questionText = q,
                    category = obs.category.ifBlank { "Other" },
                    sourceType = "Method",
                    priority = "Low",
                    context = "Evidence was noted for \"${obs.subject}\" but documentation methods could be improved.",
                    observationId = obs.id,
                    relatedCategories = listOf(obs.category)
                ))
            }
        }

        // If observations have confidence levels, ask about consistency
        val confidenceLevels = observations.map { it.confidenceLevel }.distinct()
        if (confidenceLevels.size > 1) {
            val q = "How can observation confidence levels be made more consistent across entries?"
            if (q.lowercase() !in existing) {
                results.add(GeneratedQuestion(
                    questionText = q,
                    category = "General",
                    sourceType = "Method",
                    priority = "Low",
                    context = "Observations use various confidence levels: ${confidenceLevels.joinToString(", ")}."
                ))
            }
        }

        return results
    }
}
