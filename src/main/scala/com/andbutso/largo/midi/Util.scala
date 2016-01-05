package com.andbutso.largo.midi

object Util {
  def toBinaryString(byte: Byte) = {
    String.format("%8s", Integer.toBinaryString(byte & 0xFF)).replace(' ', '0')
  }
}