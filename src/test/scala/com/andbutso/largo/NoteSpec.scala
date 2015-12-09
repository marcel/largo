package com.andbutso.largo

class NoteSpec extends ParentSpec {
  "Note" should {
    import Pitch._

    "determine what note is the specified interval away" in {
      C(0)      + 1   mustEqual(C♯(0))
      C♯(0)    + -1  mustEqual C(0)
      A(5)      + -12 mustEqual A(4)
      C(5)      + 1   mustEqual(C♯(5))
      C♯(5)    + -1  mustEqual C(5)
      D♭(5)    + 1   mustEqual D(5)
      D♭(5)    + -1  mustEqual C(5)
      B(4)      + 1   mustEqual C(5)
      C(5)      + -1  mustEqual B(4)
      C(0)      + 107 mustEqual B(8)
      B(8)      + -1  mustEqual(A♯(8))
      A(5) + A(5).interval(C(4)) mustEqual C(4)
    }

    "calculate distance in semi tones from another note" in {
      A(5) interval A(5) mustEqual SemiTone(0)
      B(4) interval C(5) mustEqual SemiTone(1)
      C(5) interval B(4) mustEqual SemiTone(-1)
      A(5) interval C(5) mustEqual SemiTone(-9)
      A(5) interval A(4) mustEqual SemiTone(-12)
      B(5) interval C(4) mustEqual SemiTone(-23)
      C(0) interval B(8) mustEqual SemiTone(107)
    }

    "compare notes ordering" in {
      A(5) must beGreaterThan(A(4))
      A(5) must beLessThan(A(6))
      A(5) must beEqualTo(A(5))

      E(5) must beGreaterThan(D(5))
      D(5) must beLessThan(E(5))

      C♯(5) must beEqualTo(D♭(5))
    }

    "calculate its frequency" in {
      val delta = 0.01

      val expectations = Map(
        Note(C, 0)   -> 16.35,
        Note(C♯, 0) -> 17.32,
        Note(D♭, 0) -> 17.32,
        Note(A, 0)   -> 27.50,
        Note(A, 1)   -> 55.0,
        Note(A, 2)   -> 110.0,
        Note(A, 3)   -> 220.0,
        Note(A, 4)   -> 440.0,
        Note(A, 5)   -> 880.0,
        Note(A, 6)   -> 1760.0,
        Note(A, 7)   -> 3520.0,
        Note(A, 8)   -> 7040.0,
        Note(A♯, 8) -> 7458.62,
        Note(B♭, 8) -> 7458.62,
        Note(B, 8)   -> 7902.13
      )

      expectations foreach { case (note, frequency) =>
        note.toFrequency must beCloseTo(frequency, delta)
      }

      ok
    }
  }
}
