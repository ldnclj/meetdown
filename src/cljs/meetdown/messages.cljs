(ns meetdown.messages)

(defrecord ChangeEventName [name])

(defrecord ChangeEventSpeaker [speaker])

(defrecord ChangeEventDescription [description])

(defrecord CreateEvent [event])

(defrecord CreateEventResults [body])
