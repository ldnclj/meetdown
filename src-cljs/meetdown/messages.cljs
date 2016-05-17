(ns meetdown.messages)

(defrecord ChangeEventName [name])

(defrecord ChangeEventSpeaker [speaker])

(defrecord ChangeEventDescription [description])

(defrecord ChangeEvent [name speaker description date postcode])

(defrecord ChangeLocation [postCode])

(defrecord CreateLocationResults [location])

(defrecord CreateEvent [event])

(defrecord CreateLocation [location])

(defrecord CreateEventResults [body])

(defrecord FindEvent [id])

(defrecord FindEventResults [body])

(defrecord FindLocation [id])

(defrecord FindLocationResults [body])
