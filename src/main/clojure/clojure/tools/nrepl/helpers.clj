(ns #^{:doc ""
       :author "Chas Emerick"}
  clojure.tools.nrepl.helpers
  (:import (java.io File StringReader)))

(defn escape
  [#^String s]
  (.replace s "\"" "\\\""))

(defn string-argument
  [s]
  (str \" (escape s) \"))

(defn load-file-command
  "Returns a string expression that can be sent to an nREPL session to
   load the code in given local file in the remote REPL's environment,
   preserving debug information (e.g. line numbers, etc).

   Typical usage: ((:send connection)
                    (load-file-command (java.io.File. \"/path/to/clojure/file.clj\")))

   If appropriate, the source path from which the code is being loaded may
   be provided as well (suitably trimming the file's path to a relative one
   when loaded)."
  ([f] (load-file-command f nil))
  ([f source-root]
    (let [abspath (if (string? f) f (.getAbsolutePath f))
          source-root (cond
                          (nil? source-root) ""
                          (string? source-root) source-root
                          (instance? File source-root) (.getAbsolutePath source-root))]
      (load-file-command (slurp abspath "UTF-8")
        (if (and (seq source-root)
              (.startsWith abspath source-root))
          (-> abspath
            (.substring (count source-root))
            (.replaceAll "^[/\\\\]" ""))
          abspath)
        (-> abspath File. .getName))))
  ([code file-path file-name]
    (apply format
      "(clojure.lang.Compiler/load (java.io.StringReader. %s) %s %s)"
      (map string-argument [code file-path file-name]))))