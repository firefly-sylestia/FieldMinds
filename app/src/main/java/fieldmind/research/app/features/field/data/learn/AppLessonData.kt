package fieldmind.research.app.features.field.data.learn

/**
 * Rich in-app lesson content that renders inside the app (no external URLs).
 *
 * Each lesson is a self-contained, step-by-step guide on a field research skill
 * or app feature. Lessons are bundled as Kotlin data so they work fully offline.
 */
data class AppLesson(
    /** Unique slug for navigation and routing. */
    val slug: String,
    /** Short display title. */
    val title: String,
    /** One-line summary shown in the Learn screen card. */
    val summary: String,
    /** Estimated reading/completion time (e.g. "5 min", "10 min"). */
    val estimatedTime: String,
    /** Skill level: "Beginner", "Intermediate", or "Advanced". */
    val level: String,
    /** Icon name from MaterialSymbolIcon set. */
    val iconName: String,
    /** Ordered content blocks that make up the lesson body. */
    val sections: List<LessonSection>,
    /** Key takeaway bullet points shown at the end. */
    val keyTakeaways: List<String>,
    /** Optional practice challenge to reinforce learning. */
    val practiceChallenge: String = ""
)

/**
 * A single section within a lesson.
 */
sealed class LessonSection {
    /** A short heading for the section. */
    abstract val heading: String
}

/** A paragraph of body text. Supports **bold** and *italic* markers. */
data class TextSection(
    override val heading: String,
    val body: String
) : LessonSection()

/** A numbered step (for procedures). */
data class StepSection(
    override val heading: String,
    val steps: List<String>
) : LessonSection()

/** A bulleted list of items. */
data class BulletSection(
    override val heading: String,
    val items: List<String>
) : LessonSection()

/** A highlighted tip, warning, or important note. */
data class CalloutSection(
    override val heading: String,
    val body: String,
    /** "tip", "warning", "note", "example" */
    val calloutType: String = "tip"
) : LessonSection()

/** An example illustrating a concept. */
data class ExampleSection(
    override val heading: String,
    val scenario: String,
    val goodExample: String = "",
    val badExample: String = ""
) : LessonSection()

/** A labeled code/format block (e.g. a field note template). */
data class CodeBlockSection(
    override val heading: String,
    val code: String,
    val language: String = "text"
) : LessonSection()

/** A two-column comparison (good vs bad, before vs after). */
data class ComparisonSection(
    override val heading: String,
    val leftLabel: String,
    val rightLabel: String,
    val rows: List<Pair<String, String>>
) : LessonSection()
