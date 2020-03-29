package chord

type Quality string

const (
	QualityMajor          = Quality("maj") // Δ, M
	QualityMinor          = Quality("min") // -
	QualityDiminished     = Quality("dim") // o, °
	QualityHalfDiminished = Quality("ø")   // ø, Ø
	QualityDominant       = Quality("dom") // 7
	QualityAugmented      = Quality("aug") // +
)
