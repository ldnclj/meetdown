(ns meetdown.login.messages)

(defrecord UsernameUpdated [username])

(defrecord PasswordUpdated [password])

(defrecord Login [])

(defrecord LoginResults [success body headers])