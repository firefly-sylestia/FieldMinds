package fieldmind.research.app.features.field.data.flashcard

import fieldmind.research.app.features.field.data.analysis.DetectedPattern
import fieldmind.research.app.features.field.data.database.entity.EvidenceAttachmentEntity
import fieldmind.research.app.features.field.data.database.entity.FlashcardEntity
import fieldmind.research.app.features.field.data.database.entity.NoteEntity
import fieldmind.research.app.features.field.data.database.entity.ObservationEntity
import fieldmind.research.app.features.field.data.database.entity.ProjectEntity
import fieldmind.research.app.features.field.data.database.entity.SourceEntity
import fieldmind.research.app.features.field.data.database.entity.SpeciesEntity

/**
 * A suggested flashcard ready to be persisted.
 *
 * @property front The question or prompt.
 * @property back The answer or content.
 * @property type Source type (observation, note, source, pattern, species, etc.).
 * @property sourceId Linked source entity ID.
 * @property projectId Linked project ID.
 * @property dedupKey Deterministic key to prevent duplicate cards.
 * @property specificityScore 0.0–1.0 how specific/concrete this card is (higher = more specific).
 * @property context Supporting context or insight that explains why this card was generated.
 * @property category Observation or category this card relates to.
 */
data class GeneratedFlashcard(
    val front: String,
    val back: String,
    val type: String,
    val sourceId: Long? = null,
    val projectId: Long? = null,
    val dedupKey: String,
    val specificityScore: Float = 0.5f,
    val context: String = "",
    val category: String = ""
)

/**
 * Enhanced offline flashcard generator that creates structured, contextual
 * flashcards from observations, notes, sources, detected patterns, species
 * data, evidence gaps, and cross-references.
 *
 * Improvements over the original:
 * 1. **Pattern-aware** — Converts [DetectedPattern] results into flashcards
 * 2. **Species-aware** — Uses [SpeciesEntity] for taxonomy/conservation cards
 * 3. **Evidence-gap** — Prompts about observations without documentation
 * 4. **Cross-reference** — Links observations to projects and vice versa
 * 5. **Comparison** — Compares categories, locations, or time periods
 * 6. **Temporal depth** — Time-span-based cards for long-running subjects
 * 7. **Cause** — Weather-linked observation cards
 * 8. **Specificity scoring** — Ranks cards by concreteness, prefers high-specificity
 */
object SmartFlashcardGenerator {

    private const val MAX_CARDS = 20

    // ══════════════════════════════════════════════════════════════════
    //  Main entry point
    // ══════════════════════════════════════════════════════════════════

    /**
     * Generate flashcards from all available data sources.
     *
     * @param observations All user observations.
     * @param notes All user notes.
     * @param sources Reference sources.
     * @param species Registered species with taxonomy and conservation data.
     * @param projects Research projects.
     * @param evidenceAttachments Media evidence attached to observations.
     * @param patterns Detected patterns from [PatternDetectionEngine].
     * @param existing Existing flashcards for deduplication.
     */
    fun generateAll(
        observations: List<ObservationEntity>,
        notes: List<NoteEntity>,
        sources: List<SourceEntity>,
        species: List<SpeciesEntity> = emptyList(),
        projects: List<ProjectEntity> = emptyList(),
        evidenceAttachments: List<EvidenceAttachmentEntity> = emptyList(),
        patterns: List<DetectedPattern> = emptyList(),
        existing: List<FlashcardEntity> = emptyList()
    ): List<GeneratedFlashcard> {
        val existingKeys = existing.map { flashcardKey(it.front, it.back) }.toMutableSet()
        val results = mutableListOf<GeneratedFlashcard>()

        fun add(cards: List<GeneratedFlashcard>) {
            cards.forEach { card ->
                if (card.dedupKey !in existingKeys) {
                    results.add(card)
                    existingKeys.add(card.dedupKey)
                }
            }
        }

        // 1. Classic observation flashcards (improved templates)
        add(generateFromObservations(observations, existingKeys))

        // 2. Classic note flashcards
        add(generateFromNotes(notes, existingKeys))

        // 3. Classic source flashcards
        add(generateFromSources(sources, existingKeys))

        // 4. Pattern-aware flashcards
        add(generateFromPatterns(patterns, existingKeys))

        // 5. Species-aware flashcards (conservation, taxonomy, unobserved)
        add(generateSpeciesFlashcards(observations, species, existingKeys))

        // 6. Cross-reference flashcards (observations ↔ projects)
        add(generateCrossReferenceFlashcards(observations, projects, existingKeys))

        // 7. Evidence-gap flashcards (observations without attachments)
        add(generateEvidenceGapFlashcards(observations, evidenceAttachments, existingKeys))

        // 8. Comparison flashcards (categories, locations)
        add(generateComparisonFlashcards(observations, existingKeys))

        // 9. Temporal depth flashcards (long-running subjects)
        add(generateTemporalFlashcards(observations, existingKeys))

        // 10. Cause flashcards (weather-linked observations)
        add(generateCauseFlashcards(observations, existingKeys))

        // Sort by specificity score descending (most specific first), take top MAX
        return results
            .distinctBy { "${it.front.lowercase().trim()}:${it.back.lowercase().trim()}" }
            .sortedByDescending { it.specificityScore }
            .take(MAX_CARDS)
    }

    // ══════════════════════════════════════════════════════════════════
    //  1. Observation flashcards (enhanced with richer templates)
    // ══════════════════════════════════════════════════════════════════

    fun generateFromObservations(
        observations: List<ObservationEntity>,
        existingKeys: MutableSet<String>
    ): List<GeneratedFlashcard> {
        val results = mutableListOf<GeneratedFlashcard>()

        for (obs in observations) {
            if (results.size >= 8) break
            val subject = obs.subject.trim()
            val facts = obs.factsOnlyNotes.trim()

            if (subject.length < 2) continue

            // Card 1: Subject → Facts (What did you observe?)
            val answer1 = facts.take(300).ifBlank { obs.evidenceSummary.take(200).ifBlank { obs.moodOrContext.take(200) } }
            if (answer1.length >= 10) {
                val key = dedupKey("obs:what", subject, answer1)
                if (key !in existingKeys) {
                    val hasWeather = obs.weatherCondition.isNotBlank()
                    val hasLocation = obs.manualLocation.isNotBlank()
                    val context = buildString {
                        append("Observed on ${obs.date}")
                        if (hasLocation) append(" at ${obs.manualLocation}")
                        if (hasWeather) append(" during ${obs.weatherCondition}")
                        append(".")
                    }
                    results.add(GeneratedFlashcard(
                        front = "What did you observe about $subject?",
                        back = answer1,
                        type = "observation",
                        sourceId = obs.id,
                        projectId = obs.projectId,
                        dedupKey = key,
                        specificityScore = if (hasWeather && hasLocation) 0.78f else if (hasWeather || hasLocation) 0.68f else 0.55f,
                        context = context,
                        category = obs.category
                    ))
                    existingKeys.add(key)
                }
            }

            // Card 2: Category → Subject
            if (obs.category.isNotBlank() && subject.length >= 3) {
                val key = dedupKey("obs:cat", subject, obs.category)
                if (key !in existingKeys) {
                    results.add(GeneratedFlashcard(
                        front = "What category is \"$subject\" under?",
                        back = "$subject → ${obs.category}",
                        type = "observation",
                        sourceId = obs.id,
                        projectId = obs.projectId,
                        dedupKey = key,
                        specificityScore = 0.6f,
                        context = "Categorized as ${obs.category} on ${obs.date}.",
                        category = obs.category
                    ))
                    existingKeys.add(key)
                }
            }

            // Card 3: Location → Subject
            if (obs.manualLocation.isNotBlank() && subject.length >= 3) {
                val key = dedupKey("obs:loc", subject, obs.manualLocation)
                if (key !in existingKeys) {
                    results.add(GeneratedFlashcard(
                        front = "Where did you observe \"$subject\"?",
                        back = "$subject was observed at ${obs.manualLocation}",
                        type = "observation",
                        sourceId = obs.id,
                        projectId = obs.projectId,
                        dedupKey = key,
                        specificityScore = 0.72f,
                        context = "Location: ${obs.manualLocation} on ${obs.date}.",
                        category = obs.category
                    ))
                    existingKeys.add(key)
                }
            }

            // Card 4: Confidence → Subject (low-confidence review prompts)
            if (obs.confidenceLevel.equals("Not sure", ignoreCase = true) || obs.confidenceLevel.equals("Low", ignoreCase = true)) {
                val key = dedupKey("obs:confidence", subject, obs.confidenceLevel)
                if (key !in existingKeys) {
                    results.add(GeneratedFlashcard(
                        front = "Review: $subject (low confidence)",
                        back = "Confidence was marked as \"${obs.confidenceLevel}\". $facts",
                        type = "observation",
                        sourceId = obs.id,
                        projectId = obs.projectId,
                        dedupKey = key,
                        specificityScore = 0.85f,
                        context = "Low-confidence observation from ${obs.date}. Consider re-observation or expert verification.",
                        category = obs.category
                    ))
                    existingKeys.add(key)
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  2. Note flashcards
    // ══════════════════════════════════════════════════════════════════

    fun generateFromNotes(
        notes: List<NoteEntity>,
        existingKeys: MutableSet<String>
    ): List<GeneratedFlashcard> {
        val results = mutableListOf<GeneratedFlashcard>()

        for (note in notes) {
            if (results.size >= 6) break
            val title = note.title.trim()
            val body = note.body.trim()

            if (title.length < 3) continue

            // Card: Title → Body summary
            val bodySummary = body.take(300).ifBlank { note.tags.ifBlank { note.category } }
            if (bodySummary.length >= 15 || body.isBlank()) {
                val key = dedupKey("note:body", title, bodySummary.take(100))
                if (key !in existingKeys) {
                    results.add(GeneratedFlashcard(
                        front = "Notes: $title",
                        back = if (body.isNotBlank()) bodySummary else "See full note for \"$title\"",
                        type = "note",
                        sourceId = note.sourceId,
                        projectId = note.projectId,
                        dedupKey = key,
                        specificityScore = if (body.isNotBlank()) 0.6f else 0.4f,
                        context = "Note from category: ${note.category}.",
                        category = note.category
                    ))
                    existingKeys.add(key)
                }
            }

            // Card: Category → Note title
            if (note.category.isNotBlank() && !note.category.equals("Other", ignoreCase = true)) {
                val key = dedupKey("note:cat", title, note.category)
                if (key !in existingKeys) {
                    results.add(GeneratedFlashcard(
                        front = "Which note relates to \"${note.category}\"?",
                        back = "\"$title\" (${note.category})",
                        type = "note",
                        sourceId = note.sourceId,
                        projectId = note.projectId,
                        dedupKey = key,
                        specificityScore = 0.55f,
                        context = "Note about ${note.category}.",
                        category = note.category
                    ))
                    existingKeys.add(key)
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  3. Source flashcards
    // ══════════════════════════════════════════════════════════════════

    fun generateFromSources(
        sources: List<SourceEntity>,
        existingKeys: MutableSet<String>
    ): List<GeneratedFlashcard> {
        val results = mutableListOf<GeneratedFlashcard>()

        for (source in sources) {
            if (results.size >= 6) break
            val title = source.title.trim()

            if (title.length < 2) continue

            // Card 1: Title → Personal summary
            val summary = source.personalSummary.take(300)
                .ifBlank { source.keyFindings.take(300) }
                .ifBlank { source.whatThisSourceTaughtMe.take(300) }
            if (summary.length >= 10) {
                val key = dedupKey("src:summary", title, summary.take(100))
                if (key !in existingKeys) {
                    results.add(GeneratedFlashcard(
                        front = "Key takeaway from \"$title\"",
                        back = summary,
                        type = "source",
                        sourceId = source.id,
                        projectId = source.relatedProjectId,
                        dedupKey = key,
                        specificityScore = 0.65f,
                        context = "Source reliability: ${source.reliabilityScore}/5. Reading status: ${source.readingStatus}.",
                        category = source.type
                    ))
                    existingKeys.add(key)
                }
            }

            // Card 2: Author → Title
            if (source.author.isNotBlank()) {
                val key = dedupKey("src:author", title, source.author)
                if (key !in existingKeys) {
                    results.add(GeneratedFlashcard(
                        front = "Who wrote \"$title\"?",
                        back = source.author,
                        type = "source",
                        sourceId = source.id,
                        projectId = source.relatedProjectId,
                        dedupKey = key,
                        specificityScore = 0.7f,
                        context = "Written by ${source.author}.",
                        category = source.type
                    ))
                    existingKeys.add(key)
                }
            }

            // Card 3: Key findings → Title
            val findings = source.keyFindings.take(300)
            if (findings.length >= 10 && findings != summary) {
                val key = dedupKey("src:findings", title, findings.take(100))
                if (key !in existingKeys) {
                    results.add(GeneratedFlashcard(
                        front = "What findings did \"$title\" present?",
                        back = findings,
                        type = "source",
                        sourceId = source.id,
                        projectId = source.relatedProjectId,
                        dedupKey = key,
                        specificityScore = 0.68f,
                        context = "Key findings from \"$title\".",
                        category = source.type
                    ))
                    existingKeys.add(key)
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  4. Pattern-aware flashcards (from PatternDetectionEngine)
    // ══════════════════════════════════════════════════════════════════

    private fun generateFromPatterns(
        patterns: List<DetectedPattern>,
        existingKeys: MutableSet<String>
    ): List<GeneratedFlashcard> {
        if (patterns.isEmpty()) return emptyList()
        val results = mutableListOf<GeneratedFlashcard>()

        patterns.forEach { pattern ->
            when (pattern.type) {
                "repeated_subject" -> {
                    pattern.relatedSubjects.firstOrNull()?.let { subject ->
                        val front = "Pattern: $subject (${pattern.count} observations)"
                        val back = pattern.insight.ifBlank {
                            "\"$subject\" has been observed ${pattern.count} times. What environmental or seasonal factors drive the repeated observations?"
                        }
                        val key = dedupKey("pat:repeated", subject, pattern.type)
                        if (key !in existingKeys) {
                            results.add(GeneratedFlashcard(
                                front = front,
                                back = back,
                                type = "pattern",
                                dedupKey = key,
                                specificityScore = 0.85f,
                                context = "Detected pattern: repeated subject. $subject observed ${pattern.count} times.",
                                category = pattern.relatedCategories.firstOrNull() ?: "General"
                            ))
                            existingKeys.add(key)
                        }
                    }
                }
                "site_revisit" -> {
                    pattern.relatedLocations.firstOrNull()?.let { loc ->
                        val front = "Pattern: Site revisit — $loc (${pattern.count} visits)"
                        val back = pattern.insight.ifBlank {
                            "\"$loc\" has been visited ${pattern.count} times. What changes have occurred across these visits?"
                        }
                        val key = dedupKey("pat:site", loc, pattern.type)
                        if (key !in existingKeys) {
                            results.add(GeneratedFlashcard(
                                front = front,
                                back = back,
                                type = "pattern",
                                dedupKey = key,
                                specificityScore = 0.88f,
                                context = "Detected pattern: site revisit. $loc visited ${pattern.count} times.",
                                category = "Site"
                            ))
                            existingKeys.add(key)
                        }
                    }
                }
                "temporal_cluster" -> {
                    val label = pattern.label.removePrefix("Peak observation time: ")
                    val front = "Pattern: Peak observation time"
                    val back = "Observations are concentrated around $label. Is this when target species are most active or when you are most available?"
                    val key = dedupKey("pat:temporal", label, pattern.type)
                    if (key !in existingKeys) {
                        results.add(GeneratedFlashcard(
                            front = front,
                            back = back,
                            type = "pattern",
                            dedupKey = key,
                            specificityScore = 0.8f,
                            context = pattern.insight.ifBlank { pattern.description },
                            category = "Timing"
                        ))
                        existingKeys.add(key)
                    }
                }
                "weather_correlation" -> {
                    val weatherLabel = pattern.label.removePrefix("Mostly ")
                    val front = "Pattern: Weather correlation"
                    val back = "How does $weatherLabel weather affect which species or phenomena are observable?"
                    val key = dedupKey("pat:weather", weatherLabel, pattern.type)
                    if (key !in existingKeys) {
                        results.add(GeneratedFlashcard(
                            front = front,
                            back = back,
                            type = "pattern",
                            dedupKey = key,
                            specificityScore = 0.75f,
                            context = pattern.insight.ifBlank { pattern.description },
                            category = "Weather"
                        ))
                        existingKeys.add(key)
                    }
                }
                "observation_gap" -> {
                    val cat = pattern.relatedCategories.firstOrNull() ?: "this category"
                    val front = "Pattern: Observation gap — $cat"
                    val back = "Why has \"$cat\" not been observed recently? Is the subject seasonal, or should effort be redirected?"
                    val key = dedupKey("pat:gap", cat, pattern.type)
                    if (key !in existingKeys) {
                        results.add(GeneratedFlashcard(
                            front = front,
                            back = back,
                            type = "pattern",
                            dedupKey = key,
                            specificityScore = 0.72f,
                            context = pattern.insight.ifBlank { pattern.description },
                            category = cat
                        ))
                        existingKeys.add(key)
                    }
                }
                "category_trend" -> {
                    if (pattern.label.startsWith("Underexplored")) {
                        val cat = pattern.relatedCategories.firstOrNull() ?: "this category"
                        val front = "Pattern: Underexplored — $cat"
                        val back = "What is hindering observations in \"$cat\" — access, season, or interest?"
                        val key = dedupKey("pat:underexplored", cat, pattern.type)
                        if (key !in existingKeys) {
                            results.add(GeneratedFlashcard(
                                front = front,
                                back = back,
                                type = "pattern",
                                dedupKey = key,
                                specificityScore = 0.68f,
                                context = pattern.insight.ifBlank { pattern.description },
                                category = cat
                            ))
                            existingKeys.add(key)
                        }
                    }
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  5. Species-aware flashcards (conservation, taxonomy, unobserved)
    // ══════════════════════════════════════════════════════════════════

    private fun generateSpeciesFlashcards(
        observations: List<ObservationEntity>,
        species: List<SpeciesEntity>,
        existingKeys: MutableSet<String>
    ): List<GeneratedFlashcard> {
        if (species.isEmpty()) return emptyList()
        val results = mutableListOf<GeneratedFlashcard>()

        // Conservation status cards
        val conservationGroups = species.groupBy { it.conservationStatus }
        conservationGroups.forEach { (status, spList) ->
            if (status in listOf("Endangered", "Critically Endangered", "Vulnerable", "Near Threatened")) {
                spList.take(2).forEach { sp ->
                    val sciName = if (sp.scientificName.isNotBlank()) " (${sp.scientificName})" else ""
                    val matchingObs = observations.filter { obs ->
                        obs.subject.contains(sp.commonName, ignoreCase = true) ||
                        (sp.scientificName.isNotBlank() && obs.subject.contains(sp.scientificName.split(" ").first(), ignoreCase = true))
                    }
                    val front = "Conservation: ${sp.commonName}$sciName"
                    val back = "${sp.commonName} is listed as $status. ${if (matchingObs.isNotEmpty()) "Observed ${matchingObs.size} time(s)." else "Not yet observed in the field."}"
                    val key = dedupKey("sp:conservation", sp.commonName, status)
                    if (key !in existingKeys) {
                        results.add(GeneratedFlashcard(
                            front = front,
                            back = back,
                            type = "species",
                            projectId = sp.projectId,
                            dedupKey = key,
                            specificityScore = 0.92f,
                            context = "Conservation status: $status. ${sp.kingdom.takeIf { it.isNotBlank() }?.let { "Kingdom: $it." } ?: ""}",
                            category = "Conservation"
                        ))
                        existingKeys.add(key)
                    }
                }
            }
        }

        // Taxonomy cards (scientific name → common name)
        species.filter { it.scientificName.isNotBlank() }.take(3).forEach { sp ->
            val front = "What is the scientific name for ${sp.commonName}?"
            val back = buildString {
                append(sp.scientificName)
                if (sp.family.isNotBlank()) append(" (Family: ${sp.family})")
                if (sp.genus.isNotBlank()) append(", Genus: ${sp.genus}")
            }
            val key = dedupKey("sp:taxonomy", sp.commonName, sp.scientificName)
            if (key !in existingKeys) {
                results.add(GeneratedFlashcard(
                    front = front,
                    back = back,
                    type = "species",
                    projectId = sp.projectId,
                    dedupKey = key,
                    specificityScore = 0.82f,
                    context = "Taxonomy for ${sp.commonName}.",
                    category = "Taxonomy"
                ))
                existingKeys.add(key)
            }
        }

        // Unobserved species (in catalog but never seen in observations)
        species.filter { it.observationCount == 0 && it.commonName.isNotBlank() }.take(2).forEach { sp ->
            val sciName = if (sp.scientificName.isNotBlank()) " (${sp.scientificName})" else ""
            val front = "Unobserved species: ${sp.commonName}$sciName"
            val back = "${sp.commonName}$sciName is in the species catalog but has never been observed. ${if (sp.conservationStatus != "Not Evaluated") "Conservation status: ${sp.conservationStatus}." else "Consider targeted surveys."}"
            val key = dedupKey("sp:unobserved", sp.commonName, "")
            if (key !in existingKeys) {
                results.add(GeneratedFlashcard(
                    front = front,
                    back = back,
                    type = "species",
                    projectId = sp.projectId,
                    dedupKey = key,
                    specificityScore = 0.78f,
                    context = "In catalog but never observed. ${sp.genus.takeIf { it.isNotBlank() }?.let { "Genus: $it." } ?: ""}",
                    category = "Survey"
                ))
                existingKeys.add(key)
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  6. Cross-reference flashcards (observations ↔ projects)
    // ══════════════════════════════════════════════════════════════════

    private fun generateCrossReferenceFlashcards(
        observations: List<ObservationEntity>,
        projects: List<ProjectEntity>,
        existingKeys: MutableSet<String>
    ): List<GeneratedFlashcard> {
        if (projects.isEmpty()) return emptyList()
        val results = mutableListOf<GeneratedFlashcard>()

        projects.filter { it.status == "Active" }.forEach { project ->
            val projectObs = observations.filter { it.projectId == project.id }
            if (projectObs.isNotEmpty() && projectObs.size >= 2) {
                // Project summary card
                val categories = projectObs.map { it.category }.distinct().filterNot { it.isBlank() }
                val front = "Project: ${project.title}"
                val back = "${projectObs.size} observations across ${categories.size} categories. ${project.objective.take(200)}"
                val key = dedupKey("xref:project", project.title, "")
                if (key !in existingKeys) {
                    results.add(GeneratedFlashcard(
                        front = front,
                        back = back,
                        type = "cross-reference",
                        projectId = project.id,
                        dedupKey = key,
                        specificityScore = 0.7f,
                        context = "Project has ${projectObs.size} linked observations.",
                        category = categories.firstOrNull() ?: "General"
                    ))
                    existingKeys.add(key)
                }

                // Most-observed subject in this project
                val topSubject = projectObs
                    .filter { it.subject.isNotBlank() }
                    .groupBy { it.subject.trim().lowercase() }
                    .maxByOrNull { it.value.size }
                topSubject?.let { (_, obsList) ->
                    val name = obsList.first().subject
                    val front2 = "Top observation in \"${project.title}\": $name"
                    val back2 = "\"$name\" was observed ${obsList.size} times in this project. ${obsList.first().factsOnlyNotes.take(200)}"
                    val key2 = dedupKey("xref:top", project.title, name)
                    if (key2 !in existingKeys) {
                        results.add(GeneratedFlashcard(
                            front = front2,
                            back = back2,
                            type = "cross-reference",
                            sourceId = obsList.first().id,
                            projectId = project.id,
                            dedupKey = key2,
                            specificityScore = 0.82f,
                            context = "Most-observed subject in project \"${project.title}\": $name (${obsList.size} times).",
                            category = obsList.first().category
                        ))
                        existingKeys.add(key2)
                    }
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  7. Evidence-gap flashcards (observations without attachments)
    // ══════════════════════════════════════════════════════════════════

    private fun generateEvidenceGapFlashcards(
        observations: List<ObservationEntity>,
        attachments: List<EvidenceAttachmentEntity>,
        existingKeys: MutableSet<String>
    ): List<GeneratedFlashcard> {
        if (observations.isEmpty()) return emptyList()
        val results = mutableListOf<GeneratedFlashcard>()

        val obsWithAttachments = attachments.map { it.observationId }.toSet()
        val obsWithoutEvidence = observations
            .filter { it.id !in obsWithAttachments && it.subject.isNotBlank() }
            .sortedByDescending { it.timestamp }
            .take(2)

        obsWithoutEvidence.forEach { obs ->
            val front = "Add evidence: ${obs.subject}"
            val back = "The observation of \"${obs.subject}\" from ${obs.date} has no attached photos, audio, or files. Consider adding visual or audio evidence."
            val key = dedupKey("gap:evidence", obs.subject, obs.date)
            if (key !in existingKeys) {
                results.add(GeneratedFlashcard(
                    front = front,
                    back = back,
                    type = "evidence-gap",
                    sourceId = obs.id,
                    projectId = obs.projectId,
                    dedupKey = key,
                    specificityScore = 0.7f,
                    context = "Observation of \"${obs.subject}\" on ${obs.date} lacks attachments.",
                    category = obs.category
                ))
                existingKeys.add(key)
            }
        }

        // Low-confidence observations worth re-verifying
        observations
            .filter { it.confidenceLevel.equals("Not sure", ignoreCase = true) || it.confidenceLevel.equals("Low", ignoreCase = true) }
            .sortedByDescending { it.timestamp }
            .take(1)
            .forEach { obs ->
                val front = "Verify: ${obs.subject}"
                val back = "Confidence was marked as \"${obs.confidenceLevel}\" for \"${obs.subject}\" (${obs.date}). Consider re-observation or expert consultation."
                val key = dedupKey("gap:verify", obs.subject, obs.date)
                if (key !in existingKeys) {
                    results.add(GeneratedFlashcard(
                        front = front,
                        back = back,
                        type = "evidence-gap",
                        sourceId = obs.id,
                        projectId = obs.projectId,
                        dedupKey = key,
                        specificityScore = 0.85f,
                        context = "Low-confidence observation from ${obs.date}.",
                        category = obs.category
                    ))
                    existingKeys.add(key)
                }
            }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  8. Comparison flashcards (categories, locations)
    // ══════════════════════════════════════════════════════════════════

    private fun generateComparisonFlashcards(
        observations: List<ObservationEntity>,
        existingKeys: MutableSet<String>
    ): List<GeneratedFlashcard> {
        val results = mutableListOf<GeneratedFlashcard>()

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
            val front = "Comparison: $c1 vs $c2"
            val back = "You have ${categories[0].value} observations in \"$c1\" and ${categories[1].value} in \"$c2\". The imbalance may reflect real abundance or observation bias."
            val key = dedupKey("cmp:categories", c1, c2)
            if (key !in existingKeys) {
                results.add(GeneratedFlashcard(
                    front = front,
                    back = back,
                    type = "comparison",
                    dedupKey = key,
                    specificityScore = 0.72f,
                    context = "${categories[0].value} observations in \"$c1\" vs ${categories[1].value} in \"$c2\".",
                    category = "Comparison"
                ))
                existingKeys.add(key)
            }
        }

        // Compare two locations with 2+ observations each
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
            val front = "Comparison: $l1 vs $l2"
            val back = "You have ${locations[0].value} observations at \"$l1\" and ${locations[1].value} at \"$l2\". Comparing species composition can reveal habitat preferences."
            val key = dedupKey("cmp:locations", l1, l2)
            if (key !in existingKeys) {
                results.add(GeneratedFlashcard(
                    front = front,
                    back = back,
                    type = "comparison",
                    dedupKey = key,
                    specificityScore = 0.74f,
                    context = "${locations[0].value} observations at \"$l1\", ${locations[1].value} at \"$l2\".",
                    category = "Comparison"
                ))
                existingKeys.add(key)
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  9. Temporal depth flashcards (long-running subjects)
    // ══════════════════════════════════════════════════════════════════

    private fun generateTemporalFlashcards(
        observations: List<ObservationEntity>,
        existingKeys: MutableSet<String>
    ): List<GeneratedFlashcard> {
        val results = mutableListOf<GeneratedFlashcard>()

        // Find subjects observed across the longest time span
        val subjectTimeSpans = observations
            .filter { it.subject.isNotBlank() && it.timestamp > 0 }
            .groupBy { it.subject.trim().lowercase() }
            .mapValues { (_, obsList) ->
                val timestamps = obsList.map { it.timestamp }
                (timestamps.maxOrNull() ?: 0L) - (timestamps.minOrNull() ?: 0L)
            }
            .filter { it.value > 0 }
            .entries
            .sortedByDescending { it.value }
            .take(1)

        subjectTimeSpans.forEach { (_, spanMs) ->
            val spanDays = (spanMs / 86_400_000L).toInt()
            if (spanDays >= 7) {
                val subjectEntry = observations
                    .filter { it.subject.isNotBlank() }
                    .groupBy { it.subject.trim().lowercase() }
                    .entries.firstOrNull { e ->
                        val timestamps = e.value.map { it.timestamp }
                        (timestamps.maxOrNull() ?: 0L) - (timestamps.minOrNull() ?: 0L) == spanMs
                    }
                subjectEntry?.let { (_, obsList) ->
                    val displayName = obsList.first().subject
                    val count = obsList.size
                    val front = "Temporal: $displayName ($spanDays days)"
                    val back = "\"$displayName\" has been observed $count times over $spanDays days. How has it changed over this period?"
                    val key = dedupKey("temp:span", displayName, "$spanDays")
                    if (key !in existingKeys) {
                        results.add(GeneratedFlashcard(
                            front = front,
                            back = back,
                            type = "temporal",
                            sourceId = obsList.first().id,
                            projectId = obsList.first().projectId,
                            dedupKey = key,
                            specificityScore = 0.84f,
                            context = "\"$displayName\" observed $count times over $spanDays days.",
                            category = obsList.first().category
                        ))
                        existingKeys.add(key)
                    }
                }
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  10. Cause flashcards (weather-linked observations)
    // ══════════════════════════════════════════════════════════════════

    private fun generateCauseFlashcards(
        observations: List<ObservationEntity>,
        existingKeys: MutableSet<String>
    ): List<GeneratedFlashcard> {
        val results = mutableListOf<GeneratedFlashcard>()

        // Weather-linked observation cards
        val weatherObs = observations.filter { it.weatherCondition.isNotBlank() && it.subject.isNotBlank() }
        weatherObs.take(2).forEach { obs ->
            val front = "${obs.subject} during ${obs.weatherCondition}"
            val back = "Observed during ${obs.weatherCondition} on ${obs.date}. Does this weather condition influence the presence or behavior of \"${obs.subject}\"? Temperature: ${obs.weatherTemperature?.let { "${it}°C" } ?: "N/A"}. Humidity: ${obs.weatherHumidity?.let { "${it}%" } ?: "N/A"}."
            val key = dedupKey("cause:weather", obs.subject, obs.weatherCondition)
            if (key !in existingKeys) {
                results.add(GeneratedFlashcard(
                    front = front,
                    back = back,
                    type = "cause",
                    sourceId = obs.id,
                    projectId = obs.projectId,
                    dedupKey = key,
                    specificityScore = 0.8f,
                    context = "Weather-condition-linked observation on ${obs.date}.",
                    category = obs.category
                ))
                existingKeys.add(key)
            }
        }

        return results
    }

    // ══════════════════════════════════════════════════════════════════
    //  Deduplication helpers
    // ══════════════════════════════════════════════════════════════════

    /**
     * Create a deterministic deduplication key so we never create the same card twice.
     */
    private fun dedupKey(prefix: String, front: String, back: String): String {
        val normalized = "$prefix:${front.lowercase().trim()}:${back.lowercase().trim()}"
        return normalized.hashCode().toLong().let {
            if (it == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(it)
        }.toString(36)
    }

    private fun flashcardKey(front: String, back: String): String {
        val normalized = "${front.lowercase().trim()}:${back.lowercase().trim()}"
        return normalized.hashCode().toLong().let {
            if (it == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(it)
        }.toString(36)
    }

}
