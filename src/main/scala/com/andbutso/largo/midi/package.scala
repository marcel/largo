package com.andbutso.largo

import com.andbutso.largo

package object midi {
  implicit def fromNoteToMidiNote(note: largo.Note): midi.Note = {
    midi.Note.fromFrequency(note.toFrequency)
  }

  implicit def fromMidiNoteToNote(note: midi.Note): largo.Note = {
    // TODO Implement fromMidiNoteToNote
    null
  }
}
