package com.andbutso

package object largo {
  type Hertz = Double

  object Frequency {
    def logarithmicRatio(frequencyA: Hertz, frequencyB: Hertz) = {
      Math.log(frequencyB / frequencyA) / NaturalLogarithm
    }

    private[this] val NaturalLogarithm = Math.log(2)
  }
}
