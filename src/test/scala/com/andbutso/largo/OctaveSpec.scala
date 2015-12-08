package com.andbutso.largo

class OctaveSpec extends ParentSpec {
  "Octave" should {
    "calculate interval between octaves" in {
      Octave(5).interval(Octave(6)) mustEqual 1
      Octave(5).interval(Octave(5)) mustEqual 0
      Octave(5).interval(Octave(4)) mustEqual -1
      Octave(5).interval(Octave(3)) mustEqual -2
      Octave(5).interval(Octave(0)) mustEqual -5
    }
  }
}
