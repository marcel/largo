package chord

type IntervalNumber string

const (
	Third         = IntervalNumber("")
	Triad         = Third
	Fifth         = IntervalNumber("⁵")
	Indeterminate = Fifth
	Neutral       = Fifth
	Sixth         = IntervalNumber("⁶")
	Seventh       = IntervalNumber("⁷")
	Ninth         = IntervalNumber("⁹")
	Eleventh      = IntervalNumber("¹¹")
	Thirteenth    = IntervalNumber("¹³")
)
