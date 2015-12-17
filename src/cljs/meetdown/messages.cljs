(ns meetdown.messages)

(defrecord ChangeEventName [name])

(defrecord ChangeEventSpeaker [speaker])

(defrecord CreateEvent [event])

(defrecord CreateEventResults [body])
