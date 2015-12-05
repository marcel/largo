package com.andbutso.largo

class PitchClassSpec extends ParentSpec {
  "PitchClass" should {
    import Pitch._

    val perfectFifth = PitchInterval.perfectFifth

    "moving up by a specified interval" in {
      "when it doesn't fall off the end of the pitch class" in {
        PitchClass.moveUpFrom(C, perfectFifth) mustEqual(G)
      }

      "when it falls off the end of the pitch class" in {
        "wraps around correctly" in {
          PitchClass.moveUpFrom(B, perfectFifth) mustEqual(F♯)
        }
      }
    }

    "moving down by a specified interval" in {
      "when it doesn't fall off the beginning of the pitch class" in {
        PitchClass.moveDownFrom(G, perfectFifth) mustEqual(C)
      }

      "when it falls off the beginning of the pitch class" in {
        "wraps around correctly" in {
          PitchClass.moveDownFrom(C, perfectFifth) mustEqual(F)
        }
      }
    }
  }
}