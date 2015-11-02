(ns uima-clj.uima
  (:require [taoensso.timbre :as timbre]
            [clojure.java.io :as io])
  (:import [org.apache.uima.fit.factory JCasFactory TypeSystemDescriptionFactory AnalysisEngineFactory]
           [org.apache.uima.fit.pipeline SimplePipeline]
           [org.apache.uima.fit.util JCasUtil]
           [org.apache.uima.analysis_engine AnalysisEngine]
           [org.apache.uima.cas.impl XmiCasSerializer XmiCasDeserializer Serialization]
           [java.io ByteArrayInputStream]
           ))

(timbre/refer-timbre)

(defn run-pipeline
  "Run a pipeline reading from the given collection reader and using
the vector of analysis engines given in the aes argument vector."
  [reader aes]
  (SimplePipeline/runPipeline reader (into-array AnalysisEngine aes)))

(defn type-system-description
  "Without any arguments this relies on the UIMAFit type description auto-detection described here:
https://code.google.com/p/uimafit/wiki/TypeDescriptorDetection.
Otherwise, it will return a TypeSystemDescription built from the given descriptor file."
  ([]
     (TypeSystemDescriptionFactory/createTypeSystemDescription))
  
  ([xml-descriptor-file]
   (TypeSystemDescriptionFactory/createTypeSystemDescriptionFromPath xml-descriptor-file)))

;;; Dealing with JCas objects
(defn new-jcas
  "Create a new JCas object."
  []
  (JCasFactory/createJCas))

(defn deserialize-jcas
  "Creates a JCas from a XCAS or XMI file (with extension xmi or xcas) and 
an optional type description. If no type description is provided it will 
use the uimaFIT auto-detection."
  ([type-system-description file]
     (JCasFactory/createJCas file type-system-description))

  ([file]
   (deserialize-jcas (type-system-description) file)))

(defn deserialize-xmi-jcas
  "Deserialize a JCas from an XMI string."
  ([jcas xmi-string]
   (let [is (ByteArrayInputStream. (.getBytes xmi-string))]
     (XmiCasDeserializer/deserialize is (.getCas jcas))
     jcas))

  ([xmi-string]
   (let [jcas (JCasFactory/createJCas)]
     (deserialize-xmi-jcas jcas xmi-string))))

(defn serialize-jcas
  "Serialize a JCas object to an XMI file"
  [jcas file-name]
  (with-open [os (io/output-stream file-name)]
    (XmiCasSerializer/serialize (.getCas jcas) os)))

(defn deserialize-binary-cas
  "Deserialize a CAS that was serialized using Serialization.serializeCAS or 
Serialization.serializeWithCompression"
  [cas-bytes]
  (let [jcas (JCasFactory/createJCas)
        cas (.getCas jcas)
        bais (ByteArrayInputStream. cas-bytes)
        ]
    (Serialization/deserializeCAS cas bais)
    cas))

(defn jcas-type-seq
  "Returns a seq of the given annotation types from the JCas object.
 For example: (jcas-type-seq DiseaseDisorderMention jcas)"
  [annotation-type jcas]
  (iterator-seq
   (JCasUtil/iterator jcas annotation-type)))

;;; Building analysis engines
(defmulti analysis-engine
  "Building an AnalysisEngine from an XML file or a Class."
  (fn [s & args] (class s)))

(defmethod analysis-engine java.lang.String
  ([s]
   (AnalysisEngineFactory/createEngineFromPath s nil))
  ([s args]
   (AnalysisEngineFactory/createEngineFromPath s (into-array Object args))))

(defmethod analysis-engine java.lang.Class
  ([s]
   (AnalysisEngineFactory/createEngine s nil))
  ([s args]
   (AnalysisEngineFactory/createEngine s (into-array Object args))))




