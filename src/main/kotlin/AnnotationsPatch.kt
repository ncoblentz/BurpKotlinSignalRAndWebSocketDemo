package com.nickcoblentz.montoya

import burp.api.montoya.core.Annotations

// This method patches the https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/core/Annotations.html interface to include a new "appendNotes()" method.
public fun Annotations.appendNote(newNotes : String) {
    if(notes().isNullOrEmpty())
        setNotes(newNotes)
    else
        setNotes("${notes()}, $newNotes")
}