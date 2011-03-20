(ns spdv.new.db-schema-jdbc
  (:import (java.sql DriverManager)))

(comment
  (defn -main [args]
    (Class/forName "org.h2.Driver"))

  (Class/forName "org.h2.Driver")

  (with-open [conn (DriverManager/getConnection "jdbc:h2:tcp://localhost/~/test" "sa" "")]
    (let [stmt (doto (.createStatement conn)
                 (.executeUpdate "CREATE TABLE hello (id BIGINT IDENTITY PRIMARY KEY);")
                 (.executeUpdate "INSERT INTO hello VALUES ();"))
          rs (doto (.executeQuery stmt "SELECT * FROM hello;")
               (map println))]
      (println rs)))

  (-main nil))


