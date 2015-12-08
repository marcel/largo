package com.andbutso.largo

class PitchSpec extends ParentSpec {
  "Pitch" should {
    import Pitch._

    "calculate interval between pitches" in {
      C.interval(C♯)     mustEqual SemiTone(1)
      (C♯).interval(C)   mustEqual SemiTone(-1)
      (G♭).interval(D♭) mustEqual SemiTone(-5)
      C.interval(B)       mustEqual SemiTone(11)
      B.interval(C)       mustEqual SemiTone(-11)
      B.interval(C♯)     mustEqual SemiTone(-10)
      B.interval(D♭)     mustEqual SemiTone(-10)

      C.interval(C)       mustEqual SemiTone(0)
      (C♯).interval(D♭) mustEqual SemiTone(0)
      (G♯).interval(A♭) mustEqual SemiTone(0)
    }
  }
}
