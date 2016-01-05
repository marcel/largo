package com.andbutso.largo

class CircularPitchClassSpec extends ParentSpec {
  "CircularPitchClass" should {
    import Pitch._

    val perfectFifth = PitchInterval.perfectFifth

    "moving up by a specified interval" in {
      "when it doesn't fall off the end of the pitch class" in {
        CircularPitchClass.moveUpFrom(C, perfectFifth) mustEqual(G)
      }

      "when it falls off the end of the pitch class" in {
        "wraps around correctly" in {
          CircularPitchClass.moveUpFrom(B, perfectFifth) mustEqual(F♯)
        }
      }
    }

    "moving down by a specified interval" in {
      "when it doesn't fall off the beginning of the pitch class" in {
        CircularPitchClass.moveDownFrom(G, perfectFifth) mustEqual(C)
      }

      "when it falls off the beginning of the pitch class" in {
        "wraps around correctly" in {
          CircularPitchClass.moveDownFrom(C, perfectFifth) mustEqual(F)
        }
      }
    }
  }
}