# ldnclj-datomic

I was disappointed with how confusing it seemed to get started with Datomic in my first meetup,
so I had a think about it and reali

## Usage

```
$ lein repl
nREPL server started on port 51539 on host 127.0.0.1 - nrepl://127.0.0.1:51539
user=> (require '[meetdown.core])
nil
user=> (in-ns 'meetdown.core)
#object[clojure.lang.Namespace 0x3a6cb9eb "meetdown.core"]
meetdown.core=> (create-dev-system)
#object[meetdown.core$create_dev_system$fn__15391 0x535dd914 "meetdown.core$create_dev_system$fn__15391@535dd914"]
meetdown.core=> (y/start!)
```

Then browse to http://localhost:8090/

## License

Copyright © 2015 p14n

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
