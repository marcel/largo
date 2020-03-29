package note

type Duration float32

const (
	Large                = Duration(8)
	Long                 = Duration(4)
	DoubleWhole          = Duration(2)
	Whole                = Duration(1)
	Half                 = Duration(1 / 2.0)
	Quarter              = Duration(1 / 4.0)  // ♩
	Eighth               = Duration(1 / 8.0)  // ♪, ♫
	Sixteenth            = Duration(1 / 16.0) // ♬
	ThirtySecond         = Duration(1 / 32.0)
	SixtyFourth          = Duration(1 / 64.0)
	HundredTwentyEighth  = Duration(1 / 128.0)
	TwoHundredFiftySixth = Duration(1 / 256.0)
)
