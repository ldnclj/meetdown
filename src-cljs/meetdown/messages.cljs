(ns meetdown.messages)

(defrecord ChangeEventName [name])

(defrecord ChangeEventSpeaker [speaker])

(defrecord ChangeEventDescription [description])

(defrecord CreateEvent [event])

(defrecord CreateEventResults [body])

(defrecord FindEvent [id])

(defrecord FindEventResults [body])
