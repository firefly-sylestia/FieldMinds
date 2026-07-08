package fieldmind.research.app.features.field.data.learn

/**
 * Bundled in-app lessons on field research skills.
 * These render inside the app — no external URLs, no internet needed.
 */
object FieldSkillsLessons {

    // ══════════════════════════════════════════════════════════════════
    //  LESSON 1: Observation Basics
    // ══════════════════════════════════════════════════════════════════

    private val observationBasics = AppLesson(
        slug = "observation-basics",
        title = "Observation Basics",
        summary = "Learn to observe like a field researcher — separate facts from interpretation and capture what matters.",
        estimatedTime = "8 min",
        level = "Beginner",
        iconName = "visibility",
        sections = listOf(
            TextSection(
                heading = "What is field observation?",
                body = "Field observation is the foundation of all research. It means carefully noticing and recording what you see, hear, smell, and feel — **without** jumping to conclusions. The goal is to capture objective data that others can verify.\n\nIn FieldMind, every observation you make starts with a subject and a category. But the real skill is in how you describe what you observed."
            ),
            CalloutSection(
                heading = "The golden rule",
                body = "Record what you observe, not what you think it means. Save interpretations for the separate notes field.",
                calloutType = "tip"
            ),
            StepSection(
                heading = "The 5-step observation method",
                steps = listOf(
                    "Identify your subject — What are you observing? Be specific. \"Red-tailed hawk\" not \"bird\".",
                    "Describe the context — Date, time, location, weather. FieldMind captures most of this automatically.",
                    "Record the facts — What did it look like? What was it doing? How many? Use measurements when possible.",
                    "Note the evidence — Take a photo, record audio, or describe tracks/signs.",
                    "Separate facts from interpretation — Keep your \"facts only\" notes objective. Save \"I think...\" for context."
                )
            ),
            ExampleSection(
                heading = "Facts vs. interpretation",
                scenario = "You see a bird with a broken wing on the ground.",
                goodExample = "Subject: Sparrow (unidentified). Behavior: Sitting on ground, left wing drooping, not flying when approached within 2m. No visible blood. Location: Edge of mixed forest, near oak tree. Time: 14:30, sunny, 22°C.",
                badExample = "Subject: Injured bird. I think it was attacked by a cat. It looks sad and probably won't survive."
            ),
            TextSection(
                heading = "Why this matters",
                body = "Good observations are reusable. Years later, another researcher (or you) can read your raw notes and draw their own conclusions. If you mix in interpretations, you lose the original data.\n\nFieldMind helps by giving you separate fields for facts-only notes and mood/context. Use them deliberately."
            ),
            BulletSection(
                heading = "Tips for better observations",
                items = listOf(
                    "Use all 5 senses — sight, sound, smell, touch, and even taste (safely!)",
                    "Be specific: \"3 Eastern gray squirrels\" not \"some squirrels\"",
                    "Record immediately — memory drifts within minutes",
                    "Include negatives: \"No other birds heard in 10 minutes\" is useful data",
                    "Use consistent terminology for the same thing across observations",
                    "When in doubt, over-describe. You can always trim later."
                )
            ),
            CalloutSection(
                heading = "FieldMind shortcut",
                body = "Use the Quick Capture button on the Home screen to log observations in seconds. The auto-timestamp, location, and weather features save you time so you can focus on the details.",
                calloutType = "tip"
            )
        ),
        keyTakeaways = listOf(
            "Separate facts from interpretation at the moment of recording",
            "Use specific, measurable language in your observations",
            "Capture context — date, time, location, weather — every time",
            "Record evidence (photos, audio) whenever possible",
            "Over-describe rather than under-describe"
        ),
        practiceChallenge = "Go outside and observe one thing for 5 minutes. Write a facts-only description, then a separate interpretation paragraph. Compare them."
    )

    // ══════════════════════════════════════════════════════════════════
    //  LESSON 2: Field Note-Taking
    // ══════════════════════════════════════════════════════════════════

    private val fieldNoteTaking = AppLesson(
        slug = "field-note-taking",
        title = "Field Note-Taking",
        summary = "Take notes that are still useful years later — structure, consistency, and the Grinnell method adapted for digital.",
        estimatedTime = "10 min",
        level = "Beginner",
        iconName = "edit_note",
        sections = listOf(
            TextSection(
                heading = "Why structured notes matter",
                body = "A good field notebook is your most important research tool. It preserves observations, captures context, and lets you (or others) verify findings long after the moment passes.\n\nNaturalist Joseph Grinnell developed a rigorous field note system in the early 1900s that field biologists still use today. FieldMind modernizes this approach for digital capture."
            ),
            TextSection(
                heading = "The Grinnell method — adapted for FieldMind",
                body = "The classic Grinnell system has three parts:\n\n1. **Journal** — Daily narrative of what you did and observed\n2. **Species accounts** — Detailed entries for each species encountered\n3. **Catalog** — Numbered list of specimens or evidence collected\n\nFieldMind adapts this: Observations are your journal entries, Species Registry is your species accounts, and Evidence Attachments are your catalog."
            ),
            StepSection(
                heading = "How to structure each note",
                steps = listOf(
                    "Start with the essentials: Subject, date, time, location — FieldMind auto-fills these",
                    "Describe the site: Habitat type, terrain, vegetation, recent weather",
                    "Record your route or search pattern: Where did you go? How long did you look?",
                    "Log each observation separately: One subject per observation entry for clarity",
                    "Add evidence: Photos, audio recordings, sketches — a picture can capture details words miss",
                    "End with a brief summary: What was the most notable thing? What surprised you?"
                )
            ),
            CodeBlockSection(
                heading = "Example: A well-structured field note in FieldMind",
                code = """
Subject: Pileated Woodpecker
Category: Birds
Date: 2024-04-15 | Time: 07:30
Location: Maple Ridge Trail, 200m from trailhead
Weather: 8°C, partly cloudy, light breeze NW

Facts-only notes:
- Large woodpecker (~40cm), mostly black with white stripes on face and neck
- Bright red crest (male — red extends to forehead)
- Heard distinctive drumming before sighting: loud, 15 rapid beats
- Observed foraging on dead oak tree trunk, ~4m up
- Pecked 8 times, paused, moved up trunk, pecked again
- Present for approximately 4 minutes before flying SE
- No other woodpeckers heard during observation

Evidence: Photo taken (side view), audio recording of drumming (30 sec)
                """.trimIndent(),
                language = "text"
            ),
            ComparisonSection(
                heading = "Good vs. weak notes",
                leftLabel = "Strong note",
                rightLabel = "Weak note",
                rows = listOf(
                    "Subject: Pileated Woodpecker, male" to "A big woodpecker",
                    "Size: ~40cm, with measurements" to "It was pretty big",
                    "Behavior: Foraging, 8 pecks, paused, moved up" to "Eating",
                    "Duration: ~4 minutes" to "I watched it for a while",
                    "Site: Dead oak, 4m up, SE edge" to "In a tree"
                )
            ),
            CalloutSection(
                heading = "Digital advantage",
                body = "FieldMind's auto-capture (timestamp, GPS, weather) means you never forget the context. Focus your mental energy on the subject details.",
                calloutType = "tip"
            ),
            BulletSection(
                heading = "Common mistakes to avoid",
                items = listOf(
                    "Writing interpretations as facts — \"It was hunting\" vs \"It caught an insect\"",
                    "Using vague language — \"several\" vs \"6\", \"warm\" vs \"28°C\"",
                    "Forgetting to record search effort — \"Didn't see any frogs\" is only useful if you note how you looked",
                    "Skipping dates or locations — even familiar sites need documentation",
                    "Not recording negatives — \"no tracks found\" is valuable data"
                )
            )
        ),
        keyTakeaways = listOf(
            "Structure every note the same way for consistency",
            "Include site description, route, and search effort",
            "One subject per observation entry",
            "Record negatives — they're as important as positives",
            "Use FieldMind's auto-fields to save time on context"
        ),
        practiceChallenge = "Pick a spot you visit regularly (your backyard, a local park). Make three observations over a week, following the same structure each time. Compare them — what patterns emerge?"
    )

    // ══════════════════════════════════════════════════════════════════
    //  LESSON 3: Identifying Bias
    // ══════════════════════════════════════════════════════════════════

    private val identifyingBias = AppLesson(
        slug = "identifying-bias",
        title = "Identifying Bias in Field Observations",
        summary = "Spot and minimize the biases that distort every observer's data — confirmation bias, observer bias, and sampling bias.",
        estimatedTime = "12 min",
        level = "Intermediate",
        iconName = "balance",
        sections = listOf(
            TextSection(
                heading = "What is bias?",
                body = "Bias is a systematic error that skews your observations away from the truth. Every observer has biases — they're part of being human. The goal isn't to eliminate bias entirely (impossible), but to **recognize** it and **minimize** its effects.\n\nIn field research, three types of bias are especially important:"
            ),
            TextSection(
                heading = "1. Confirmation bias",
                body = "Confirmation bias is the tendency to notice, remember, and favor information that confirms what you already believe.\n\n**Example:** You think woodpeckers are rare in your area. You see one — but dismiss it as \"probably a flicker.\" Later, you tell others woodpeckers are rare, forgetting the sighting.\n\n**In FieldMind:** You might unconsciously choose categories or tags that fit your existing mental model rather than what you actually observed."
            ),
            CalloutSection(
                heading = "How to counter confirmation bias",
                body = "Before going into the field, write down your expectation — then deliberately look for evidence that might disprove it. This is called 'considering the opposite.'",
                calloutType = "tip"
            ),
            TextSection(
                heading = "2. Observer bias",
                body = "Observer bias happens when different observers (or the same observer at different times) record the same phenomenon differently.\n\n**Example:** One person calls a color \"brownish-gray\" while another calls it \"grayish-brown.\" Or a fatigued observer misses subtle behaviors that a fresh observer catches.\n\n**In FieldMind:** Stick to consistent terminology. Use the same tags, the same confidence levels, and the same level of detail across all your observations."
            ),
            TextSection(
                heading = "3. Sampling bias",
                body = "Sampling bias occurs when your observations aren't representative of what's really out there.\n\n**Common causes:**\n- Only observing in convenient locations (near roads, trails, your house)\n- Only observing at certain times (midday, weekends, good weather)\n- Focusing on charismatic species while ignoring common ones\n- Stopping when you've seen enough"
            ),
            ComparisonSection(
                heading = "Biased vs. systematic sampling",
                leftLabel = "Biased approach",
                rightLabel = "Systematic approach",
                rows = listOf(
                    "Walk the same trail every time" to "Rotate between 3-4 different routes",
                    "Only observe in good weather" to "Sample in varied weather conditions",
                    "Record only birds, ignore plants" to "Record all categories present",
                    "Stop after 20 minutes" to "Observe for a fixed 30-minute period every time",
                    "Focus on the most colorful individuals" to "Record every individual you encounter"
                )
            ),
            StepSection(
                heading = "Minimizing bias in FieldMind",
                steps = listOf(
                    "Use consistent categories — Define your categories upfront and stick to them",
                    "Set a timer — Observe for a fixed duration each session (use the Timer tool)",
                    "Record negatives — Use FieldMind to log when you DON'T find something",
                    "Use confidence levels honestly — Mark \"Not sure\" when you mean it",
                    "Review past observations — The Insights screen can reveal your blind spots",
                    "Compare with others — Use the export/share feature to get feedback"
                )
            ),
            CalloutSection(
                heading = "FieldMind helps",
                body = "The auto-detected patterns feature can reveal observation biases you didn't notice — like that you only observe birds on weekends, or that you visit the same location 80% of the time. Check the Insights screen regularly.",
                calloutType = "note"
            ),
            ExampleSection(
                heading = "Real example",
                scenario = "A student observes butterflies only on sunny days and concludes butterflies prefer sunny weather.",
                goodExample = "Observation plan: Visit site 6 times — 3 sunny, 3 cloudy — at the same time of day. Record: Number of butterflies seen per 10-minute walk. Result: Actually more butterflies on cloudy days because it's cooler and they're less active / easier to spot. The original bias: observer only went out in nice weather.",
                badExample = "Conclusion: Butterflies like sunny weather. (Sampling bias: only observed in one condition, no comparison data.)"
            )
        ),
        keyTakeaways = listOf(
            "Bias is universal — the goal is to recognize and minimize it, not eliminate it",
            "Confirmation bias: actively seek disconfirming evidence",
            "Observer bias: use consistent terminology and methods",
            "Sampling bias: vary when, where, and what you observe",
            "Record negatives — they're essential for unbiased analysis",
            "Review your patterns regularly on the Insights screen"
        ),
        practiceChallenge = "Review your last 10 observations in FieldMind. Ask yourself: Are they all in similar locations? Similar times? Similar weather? Similar subjects? Identify one bias pattern and plan one observation session deliberately designed to counter it."
    )

    // ══════════════════════════════════════════════════════════════════
    //  LESSON 4: Species Identification
    // ══════════════════════════════════════════════════════════════════

    private val speciesIdentification = AppLesson(
        slug = "species-identification",
        title = "Species Identification",
        summary = "Use field marks, habitat, behavior, and seasonal patterns to identify species confidently.",
        estimatedTime = "10 min",
        level = "Beginner",
        iconName = "pets",
        sections = listOf(
            TextSection(
                heading = "The art of identification",
                body = "Identifying a species is like solving a puzzle. You gather clues — size, shape, color, location, behavior, sound — and narrow down possibilities until only one fits.\n\nFieldMind has a built-in Species Registry and Species Browser to help, but the real skill is knowing what to look for."
            ),
            StepSection(
                heading = "The 5-clue method",
                steps = listOf(
                    "Size & shape — Compare to familiar objects. \"Sparrow-sized\" is more useful than \"small.\" Note the overall body shape, beak shape, wing shape.",
                    "Color & pattern — Look for distinctive field marks: wing bars, eye stripes, tail patterns, belly color. Focus on one feature at a time.",
                    "Behavior — How does it move? Does it hop or walk? Does it flick its tail? Is it solitary or in a group? What is it doing?",
                    "Habitat & location — Where did you see it? Forest, grassland, wetland, urban? Different species occupy different niches. Altitude matters too.",
                    "Sound — Calls, songs, wing sounds, alarm notes. Many species are more easily identified by sound than by sight."
                )
            ),
            CodeBlockSection(
                heading = "Example identification process",
                code = """
Clue 1 — Size & shape: Robin-sized, sturdy body, strong conical beak
Clue 2 — Color & pattern: Gray-brown back, white belly, black cap on head, 
          pinkish breast, white outer tail feathers visible in flight
Clue 3 — Behavior: Ground forager, walks (doesn't hop), scratches leaf litter,
          short flights to low branches when disturbed
Clue 4 — Habitat: Mixed forest edge with dense understory, near berry bushes
Clue 5 — Sound: Clear whistled song: "cheerily cheer-up cheerio"

Identification: American Robin (Turdus migratorius)
Confidence: High — all clues consistent, no contradictions
                """.trimIndent(),
                language = "text"
            ),
            TextSection(
                heading = "Using the Species Registry",
                body = "FieldMind's Species Registry lets you build a personal catalog of species you've identified. Each entry can store:\n\n- **Common and scientific names** — Essential for clear communication\n- **Taxonomy** — Kingdom through species, helps with pattern recognition\n- **Conservation status** — Track endangered or threatened species\n- **Target count** — Set goals for observation frequency\n- **Notes** — Your own ID tips for that species"
            ),
            CalloutSection(
                heading = "When you can't identify",
                body = "It's better to record an observation as \"unidentified sparrow\" or \"unknown warbler\" than to guess wrong. Mark your confidence as \"Not sure\" and add enough description that you (or an expert) can identify it later. Use the Notes field to describe what you saw in detail.",
                calloutType = "tip"
            ),
            BulletSection(
                heading = "Common identification pitfalls",
                items = listOf(
                    "Juvenile plumage — Many young birds look nothing like adults",
                    "Seasonal variation — Breeding vs. non-breeding plumage can look like different species",
                    "Sexual dimorphism — Males and females of the same species may look completely different",
                    "Lighting conditions — Colors look different in dawn light vs. midday sun vs. shade",
                    "Distance — The further away, the more unreliable your ID"
                )
            ),
            TextSection(
                heading = "Photographing for ID",
                body = "When you can't identify a species in the field, take photos that capture:\n\n1. **Full body profile** — Side view shows overall shape and proportions\n2. **Head close-up** — Beak shape, eye color, head patterns are critical\n3. **Any distinctive feature** — Tail pattern, wing bars, feet, unusual marking\n4. **Habitat context** — A wide shot showing the environment\n\nFieldMind lets you attach multiple photos to a single observation. Use this feature!"
            )
        ),
        keyTakeaways = listOf(
            "Use the 5-clue method: size/shape, color, behavior, habitat, sound",
            "Compare to known species — note both similarities and differences",
            "Record detailed descriptions even when you can't identify",
            "Use confidence levels honestly — it's okay to be unsure",
            "Build your Species Registry as a personal reference",
            "Take diagnostic photos from multiple angles"
        ),
        practiceChallenge = "Pick an animal or plant you see frequently but have never identified. Use the 5-clue method, photograph it, and add it to your Species Registry. Then verify your ID using an external source."
    )

    // ══════════════════════════════════════════════════════════════════
    //  LESSON 5: Data Collection Methods
    // ══════════════════════════════════════════════════════════════════

    private val dataCollectionMethods = AppLesson(
        slug = "data-collection-methods",
        title = "Data Collection Methods",
        summary = "Learn the standard field methods — transects, quadrats, point counts — and when to use each in FieldMind.",
        estimatedTime = "12 min",
        level = "Intermediate",
        iconName = "bar_chart",
        sections = listOf(
            TextSection(
                heading = "Why use standard methods?",
                body = "Standardized data collection methods let you:\n- Compare results across different sites and times\n- Combine your data with other researchers' data\n- Analyze results statistically\n- Publish or share findings credibly\n\nFieldMind's data tools (Counter, Measure, Weather Log, Site Log) support these standard methods."
            ),
            TextSection(
                heading = "Method 1: Point counts",
                body = "**Best for:** Birds, audible wildlife, stationary observations\n\nStand at a fixed point and record every individual you see or hear within a set time (usually 5-10 minutes) and distance (e.g., 50m radius).\n\n**In FieldMind:** Use the Counter tool to tally individuals. Create one observation per species, or use tags to separate species within a single session."
            ),
            StepSection(
                heading = "How to run a point count",
                steps = listOf(
                    "Choose your point — preferably random or systematic, not convenient",
                    "Wait 1-2 minutes for animals to resume normal activity after your arrival",
                    "Start a timer (use the Timer tool in FieldMind) for your count duration",
                    "Record every individual detected — don't estimate, count each one",
                    "Note distance categories: 0-25m, 25-50m, 50-100m, beyond 100m",
                    "Record weather conditions — wind and rain dramatically affect detectability"
                )
            ),
            TextSection(
                heading = "Method 2: Transects (line surveys)",
                body = "**Best for:** Plants, slow-moving animals, habitat assessment, along gradients\n\nWalk a straight line of fixed length and record everything you encounter within a set distance on either side.\n\n**In FieldMind:** Use the Measure tool to record your transect length. Log observations at regular intervals (e.g., every 10m). The Site Log tool can record habitat characteristics at each point."
            ),
            CodeBlockSection(
                heading = "Transect data format",
                code = """
Transect ID: T-2024-04-15-01
Length: 100m
Width: 2m (1m each side)
Direction: 270° (due west)
Start: 47.6219°N, 122.3493°W
End: 47.6219°N, 122.3505°W
Habitat: Mixed deciduous-conifer forest, moderate understory

Point 0m: Start — dense Douglas fir canopy, little understory
Point 10m: Open gap — salal and Oregon grape, 2m tall
Point 20m: Fallen log (decaying, ~40cm diameter) — moss cover 80%
Point 30m: Stream crossing, ~1m wide, rocky bottom
...
                """.trimIndent(),
                language = "text"
            ),
            TextSection(
                heading = "Method 3: Quadrats (plots)",
                body = "**Best for:** Plants, insects, soil analysis, habitat composition\n\nMark a fixed-area plot (typically 1m² for plants, larger for trees or slow animals) and record everything inside it.\n\n**In FieldMind:** Create a Project for your study site. Use Data Records to log counts and measurements. The Survey tool helps with species abundance estimates."
            ),
            BulletSection(
                heading = "Choosing the right method",
                items = listOf(
                    "Point count — When you can stay in one place and monitor activity (bird song surveys, bat emergence counts)",
                    "Transect — When you want to sample along an environmental gradient (elevation, moisture, disturbance)",
                    "Quadrats — When you need precise density estimates (plants per m², insect larvae counts)",
                    "Opportunistic — When you're exploring a new area (use for initial surveys, but note it's not systematic)",
                    "Timed search — When comparing search effort matters (same time spent at different sites)"
                )
            ),
            CalloutSection(
                heading = "Why method matters",
                body = "The method you choose determines what questions you can answer. A point count tells you species presence/absence. A transect tells you distribution across space. A quadrat tells you density. Pick the method that fits your question.",
                calloutType = "note"
            ),
            TextSection(
                heading = "Recording method metadata in FieldMind",
                body = "For each observation session, record:\n- **Method used** (point count, transect, quadrat, opportunistic)\n- **Search effort** (time spent, distance covered, area sampled)\n- **Conditions** (weather, time of day, observer)\n- **Any deviations** from standard method (and why)\n\nUse the Context/Mood field in observations to store this metadata. Or create a dedicated Research Session for each method."
            )
        ),
        keyTakeaways = listOf(
            "Standard methods let you compare data across time and space",
            "Point counts = stationary, good for birds and audible wildlife",
            "Transects = linear, good for gradients and larger areas",
            "Quadrats = area-based, good for density estimates",
            "Always record search effort — it's essential for analysis",
            "Choose your method based on your research question, not convenience"
        ),
        practiceChallenge = "Design a mini-study using one of the three methods. Pick a 50m transect in a local area. Run it at the same time on 3 different days. Use FieldMind's Counter, Measure, and Project tools to log the data. What patterns do you see?"
    )

    // ══════════════════════════════════════════════════════════════════
    //  LESSON 6: Asking Research Questions
    // ══════════════════════════════════════════════════════════════════

    private val askingResearchQuestions = AppLesson(
        slug = "asking-research-questions",
        title = "Asking Researchable Questions",
        summary = "Turn curiosity into focused, testable questions that lead to meaningful investigation.",
        estimatedTime = "10 min",
        level = "Intermediate",
        iconName = "help",
        sections = listOf(
            TextSection(
                heading = "From curiosity to question",
                body = "Every research project starts with curiosity. You notice something interesting — a pattern, an anomaly, a puzzle. But \"Why is that happening?\" is too broad. Good research questions are **focused**, **specific**, and **answerable**.\n\nFieldMind's Question tool helps you track questions as you develop them. You can link questions to observations, hypotheses, and projects."
            ),
            TextSection(
                heading = "The FINER criteria",
                body = "A strong research question should be:\n\n- **F**easible — Can you actually answer this with your resources, time, and skills?\n- **I**nteresting — Does the answer matter to you or others?\n- **N**ovel — Does it add to what's already known?\n- **E**thical — Can you investigate this without harm?\n- **R**elevant — Does it connect to bigger questions in your field?"
            ),
            CalloutSection(
                heading = "Feasibility check",
                body = "Before committing to a question, ask: Can I observe or measure this? Do I have the equipment? The time? The access? A perfect question you can't answer is less useful than a simple question you can.",
                calloutType = "warning"
            ),
            ComparisonSection(
                heading = "Broad vs. focused questions",
                leftLabel = "Too broad",
                rightLabel = "Focused & researchable",
                rows = listOf(
                    "Why are birds dying?" to "What is the most common cause of mortality in urban songbirds near my study site during spring migration?",
                    "How does pollution affect nature?" to "How does the density of macroinvertebrates in Mill Creek differ upstream vs. downstream of the sewage treatment plant?",
                    "What plants are here?" to "What is the relative abundance of native vs. invasive plant species in the 500m riparian zone along the Blue River?",
                    "How do animals behave?" to "Does the frequency of alarm calls in Eastern Gray Squirrels increase when off-leash dogs are present in Riverside Park?"
                )
            ),
            StepSection(
                heading = "From observation to question",
                steps = listOf(
                    "Notice something — Record your initial curiosity in a FieldMind question entry",
                    "Ask \"What specifically?\" — Narrow down. Instead of \"why are there fewer bees?\" ask \"Is bee visitation rate to lavender plants lower on windy days?\"",
                    "Identify variables — What will you measure? What will you compare? What will you control?",
                    "Turn into a testable prediction — \"If wind speed is above 15 km/h, then bee visitation rate will decrease by at least 50%\"",
                    "Link to existing data — Check FieldMind's Insights screen for patterns that might inform your question",
                    "Create a hypothesis — Use the Hypothesis tool to state your prediction and plan your test"
                )
            ),
            TextSection(
                heading = "Using FieldMind's Question tool",
                body = "FieldMind has a dedicated Question tool that helps you:\n- **Track questions** as they evolve from vague to precise\n- **Link questions** to observations that inspired them\n- **Connect questions** to sources that inform them\n- **Create hypotheses** from your questions\n- **Set priority** — High, Medium, or Low\n- **Track status** — Open, In progress, Answered"
            ),
            BulletSection(
                heading = "Signs of a good question",
                items = listOf(
                    "It can be answered with data you can collect in days/weeks, not years",
                    "The key variables are clearly defined and measurable",
                    "You can state a specific prediction before collecting data",
                    "It connects to something you've already observed",
                    "The answer would genuinely change what you think or do",
                    "It's small enough to be completable but big enough to matter"
                )
            ),
            CalloutSection(
                heading = "Auto-generated questions",
                body = "FieldMind can automatically generate questions from your observations, detected patterns, species data, and evidence gaps. Enable \"Auto questions\" in Settings → Auto generation. Review them regularly — they might surface questions you hadn't thought of.",
                calloutType = "tip"
            ),
            ExampleSection(
                heading = "Question evolution example",
                scenario = "You notice that you see more robins in the morning than the afternoon.",
                goodExample = "Start: \"Why do I see more robins at certain times?\" → Refine: \"Does the foraging activity (pecks per minute) of American Robins vary by time of day?\" → Testable: \"American Robins will have higher foraging peck rates in the 2 hours after sunrise compared to the 2 hours before sunset.\" → Linked to observation data, marked as Hypothesis.",
                badExample = "Question left as: \"Why robins?\" — too vague, no variables, no possible answer path."
            )
        ),
        keyTakeaways = listOf(
            "Start with curiosity, refine with the FINER criteria",
            "Good questions are focused, specific, and answerable with your resources",
            "Identify your variables: what will you measure and compare?",
            "Turn questions into testable predictions (hypotheses)",
            "Use FieldMind's Question tool to track your question development",
            "Check auto-generated questions for inspiration"
        ),
        practiceChallenge = "Review your last 5 observations. For each one, write down a question it raises. Apply the FINER criteria. Refine the best one into a testable prediction and create a Hypothesis in FieldMind."
    )

    // ══════════════════════════════════════════════════════════════════
    //  LESSON 7: Evidence & Documentation
    // ══════════════════════════════════════════════════════════════════

    private val evidenceAndDocumentation = AppLesson(
        slug = "evidence-and-documentation",
        title = "Evidence & Documentation",
        summary = "Take useful photos, record clear audio, and collect evidence that strengthens your observations.",
        estimatedTime = "8 min",
        level = "Beginner",
        iconName = "camera_alt",
        sections = listOf(
            TextSection(
                heading = "Why evidence matters",
                body = "A well-documented observation is **verifiable**. Someone else (or you, months later) can review your evidence and confirm or challenge your conclusions. Evidence turns a claim into a data point.\n\nFieldMind supports three types of evidence: photos, audio recordings, and file attachments. Using them well dramatically increases the value of your observations."
            ),
            TextSection(
                heading = "Taking useful field photos",
                body = "A good field photo is more than a pretty picture. It should capture identifying features and context:\n\n1. **Overview** — Show the habitat and setting (step back, include surroundings)\n2. **Subject** — Get as close as possible, fill the frame\n3. **Detail** — Close-up of the key identifying feature (beak, leaf pattern, track detail)\n4. **Scale reference** — Include a familiar object for size: coin, hand, ruler, pen"
            ),
            StepSection(
                heading = "Photo checklist before you tap",
                steps = listOf(
                    "Clean the lens — A smudge ruins the shot",
                    "Steady your phone — Brace against something or use both hands",
                    "Get close — But not so close you disturb the subject",
                    "Use grid lines — Keep the subject in the rule-of-thirds intersection",
                    "Take multiple angles — Side, top, 45° angle for 3D context",
                    "Include scale — A coin, finger, or measuring tape in frame",
                    "Check the photo — Is the identifying feature visible? Retake if not"
                )
            ),
            CalloutSection(
                heading = "Night/low-light photography",
                body = "Use your phone's night mode if available. For very dark conditions, use the flash but stand further back (flash can wash out details). Consider taking a video and extracting frames — it can capture better low-light images.",
                calloutType = "tip"
            ),
            TextSection(
                heading = "Recording audio for identification",
                body = "Audio recordings are invaluable for birds, insects, amphibians, and other sound-producing species. Tips for clear recordings:\n\n- **Get close** — But don't approach so closely that you alter behavior\n- **Minimize noise** — Step away from roads, wind, running water if possible\n- **Record context first** — Say the date, time, location, and subject aloud before the sound\n- **Record enough** — 30 seconds minimum, longer for variable calls\n- **Note what you hear** — Even if you can't identify the sound, describe it"
            ),
            TextSection(
                heading = "Collecting physical evidence",
                body = "Sometimes digital documentation isn't enough. Physical evidence might include:\n\n- **Fur, feathers, or scat** — Collected in a sealed bag with location and date\n- **Soil or water samples** — In sterile containers, labeled immediately\n- **Pressed plants** — Between newspaper sheets in a plant press or heavy book\n- **Insect specimens** — In 70% ethanol or pinned in a collection box\n\n**Ethical note:** Only collect what's legal and necessary. Many areas require permits for specimen collection. Photograph rather than collect whenever possible."
            ),
            CalloutSection(
                heading = "Evidence ethics",
                body = "Always follow the 'leave no trace' principle. Don't damage habitat to get a photo. Don't collect protected species. If you're unsure about the rules for a site, contact the land manager. FieldMind's attachments let you document without taking.",
                calloutType = "warning"
            ),
            CodeBlockSection(
                heading = "Evidence log template",
                code = """
Evidence ID: E-2024-04-15-001
Type: Photograph
Subject: Unidentified hawk feather
Linked to observation: O-2024-04-15-003
Date collected: 2024-04-15
Location: Maple Ridge Trail, ~200m from trailhead
Collector: Self
Description: Primary flight feather, approx. 35cm, brown with dark banding,
  found on trail surface near base of large Douglas fir
Storage: Sealed ziplock bag, labeled with ID and date
                """.trimIndent(),
                language = "text"
            ),
            BulletSection(
                heading = "Best practices summary",
                items = listOf(
                    "Every observation should have at least one piece of evidence if possible",
                    "Label everything immediately — memory is unreliable",
                    "Take more photos than you think you need — delete later",
                    "Record audio descriptions if you can't write notes (FieldMind supports this!)",
                    "For physical samples, include a scale reference in photos before collecting",
                    "Back up regularly — FieldMind's export feature keeps evidence safe"
                )
            )
        ),
        keyTakeaways = listOf(
            "Evidence makes observations verifiable and more valuable",
            "Take three photos per subject: overview, close-up, detail with scale",
            "Record audio with context — say the date, location, and subject first",
            "Collect physical evidence ethically and legally",
            "Label everything immediately",
            "Use FieldMind's attachment feature for every observation"
        ),
        practiceChallenge = "Find an object outside (a leaf, a feather, a rock, an insect). Document it using ALL three types of evidence: a photo with scale, a 30-second audio description, and a written FieldMind observation with a detailed facts-only note."
    )

    // ══════════════════════════════════════════════════════════════════
    //  Aggregators (defined last so all lesson vals are initialized)
    // ══════════════════════════════════════════════════════════════════

    val allLessons: List<AppLesson> = listOf(
        observationBasics,
        fieldNoteTaking,
        identifyingBias,
        speciesIdentification,
        dataCollectionMethods,
        askingResearchQuestions,
        evidenceAndDocumentation
    )

    val bySlug: Map<String, AppLesson> = allLessons.associateBy { it.slug }
}
