(ns meetdown.login.messages)

(defrecord UsernameUpdated [username])

(defrecord PasswordUpdated [password])

(defrecord Login [])

(defrecord LoginResults [success body headers])

(defrecord LoginSuccess [body headers])

(defrecord LoginFailure [errors])

(defrecord Logout [])