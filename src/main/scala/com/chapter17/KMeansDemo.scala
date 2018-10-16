package com.chapter17

import org.apache.log4j.{Level, LogManager}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._

/*
 * @author Md. Rezaul Karim
 */

object KMeansDemo {

  // Class that reads all the features as double
  case class Land(Price: Double, LotSize: Double, Waterfront: Double, Age: Double, LandValue: Double, NewConstruct: Double, CentralAir: Double, FuelType: Double, HeatType: Double, SewerType: Double, LivingArea: Double, PctCollege: Double, Bedrooms: Double, Fireplaces: Double, Bathrooms: Double, rooms: Double)

  // function to create a  Land class from an Array of Double
  def parseLand(line: Array[Double]): Land = {
    Land(
      line(0), line(1), line(2), line(3), line(4), line(5),
      line(6), line(7), line(8), line(9), line(10),
      line(11), line(12), line(13), line(14), line(15))
  }

  // function to transform an RDD of Strings into an RDD of Double
  def parseRDD(rdd: RDD[String]): RDD[Array[Double]] = {
    rdd.map(_.split(",")).map(_.map(_.toDouble))
  }

  def main(args: Array[String]): Unit = {

    val log = LogManager.getRootLogger
    log.setLevel(Level.WARN)
    //log.trace("Applicaiton started")
    log.warn("Started")

    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName("KMeans")
      .getOrCreate()

    //import spark.sqlContext._
    import spark.sqlContext.implicits._

    //Start parsing the dataset
    val start = System.currentTimeMillis()
    val dataPath = "Saratoga_NY_Homes.txt"
    //val dataPath = args(0)
    val landDF = parseRDD(spark.sparkContext.textFile(dataPath)).map(parseLand).toDF().cache()
    landDF.show()

    // convert back to rdd and cache the data for creating a new data frame to link the cluster number
    val rowsRDD = landDF.rdd.map(r => (r.getDouble(0), r.getDouble(1), r.getDouble(2), r.getDouble(3), r.getDouble(4), r.getDouble(5), r.getDouble(6), r.getDouble(7), r.getDouble(8), r.getDouble(9), r.getDouble(10), r.getDouble(11), r.getDouble(12), r.getDouble(13), r.getDouble(14), r.getDouble(15)))
    rowsRDD.cache()
    //rowsRDD.coalesce(1, true).saveAsTextFile("data/output")

    // convert back to rdd and cache the data for creating training the KMeans model
    val landRDD = landDF.rdd.map(r => Vectors.dense(r.getDouble(1), r.getDouble(2), r.getDouble(3), r.getDouble(4), r.getDouble(5), r.getDouble(6), r.getDouble(7), r.getDouble(8), r.getDouble(9), r.getDouble(10), r.getDouble(11), r.getDouble(12), r.getDouble(13), r.getDouble(14), r.getDouble(15)))
    landRDD.cache()
    //val dfwithIndex = landRDD.zipWithIndex

    // Cluster the data into two classes using KMeans
    val numClusters = 5
    val numIterations = 20
    val seed = 12345
    val runs = 50
    val model = KMeans.train(landRDD, numClusters, numIterations, seed)
    //val model = KMeans.

    // Evaluate clustering by computing Within Set Sum of Squared Errors
    val WCSSS = model.computeCost(landRDD)
    println("Within-Cluster Sum of Squares = " + WCSSS) // Less is better

    val end = System.currentTimeMillis()
    println("Model building and prediction time: " + { end - start } + "ms")

    // Compute and print the prediction accuracy for each house
    model.predict(landRDD).foreach(println)
    landDF.show()

    // Get the prediction from the model with the ID so we can link them back to other information
    val predictions = rowsRDD.map { r => (r._1, model.predict(Vectors.dense(r._2, r._3, r._4, r._5, r._6, r._7, r._8, r._9, r._10, r._11, r._12, r._13, r._14, r._15, r._16))) }
    val conMat = predictions.collect().toMap.values
    println(conMat)

    // convert the rdd to a dataframe
    val predCluster = predictions.toDF("Price", "CLUSTER")
    predCluster.show()

    // Join the prediction DataFrame with the old dataframe to know the individual cluster number for each house
    val newDF = landDF.join(predCluster, "Price")
    newDF.show()

    // Review a subset of each cluster
    newDF.filter("CLUSTER = 0").show()
    newDF.filter("CLUSTER = 1").show()
    newDF.filter("CLUSTER = 2").show()
    newDF.filter("CLUSTER = 3").show()
    newDF.filter("CLUSTER = 4").show()

    // Get the descriptive statistics for each cluster
    newDF.filter("CLUSTER = 0").describe().show()
    newDF.filter("CLUSTER = 1").describe().show()
    newDF.filter("CLUSTER = 2").describe().show()
    newDF.filter("CLUSTER = 3").describe().show()
    newDF.filter("CLUSTER = 4").describe().show()

    landDF.show()
    predCluster.show()
    println(conMat)
    log.warn("Finshed")

    // Create a tem table
    newDF.createOrReplaceTempView("land")
    val dfTemp = spark.sqlContext.sql("SELECT * from land")

    newDF.show()

    //Finally, stop the Spark context
    spark.stop()
  }
}
