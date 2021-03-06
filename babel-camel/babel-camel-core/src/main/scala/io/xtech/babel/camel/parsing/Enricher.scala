/*
 *
 *  Copyright 2010-2014 Crossing-Tech SA, EPFL QI-J, CH-1015 Lausanne, Switzerland.
 *  All rights reserved.
 *
 * ==================================================================================
 */

package io.xtech.babel.camel.parsing

import io.xtech.babel.camel.model._
import io.xtech.babel.camel.{ CamelDSL, EnricherDSL }
import io.xtech.babel.fish.BaseDSL
import io.xtech.babel.fish.parsing.StepInformation
import org.apache.camel.model.{ EnrichDefinition => CamelEnrichDefinition, PollEnrichDefinition => CamelPollEnrichDefinition, ProcessorDefinition }
import org.apache.camel.processor.aggregate.AggregationStrategy

import scala.collection.immutable
import scala.language.implicitConversions
import scala.reflect.ClassTag

/**
  * Defines the enrich and pollEnrich keyword in the DSL.
  */
private[babel] trait Enricher extends CamelParsing {
  self: CamelDSL =>

  abstract override protected def steps: immutable.Seq[Process] = super.steps :+ parse

  /**
    * Parsing of the enricher feature
    */
  private[this] def parse: Process = {

    // parsing of the enrichRef keyword
    case StepInformation(step @ EnrichDefinition(CamelSink(resourceUri), aggregationRef), camelProcessorDefinition: ProcessorDefinition[_]) => {

      val camelEnrichDefinition = aggregationRef match {
        case Left(ref) =>
          val definition = new CamelEnrichDefinition(resourceUri)
          definition.setAggregationStrategyRef(ref)
          definition
        case Right(strategy) =>
          new CamelEnrichDefinition(strategy, resourceUri)
      }

      camelProcessorDefinition.addOutput(camelEnrichDefinition)
      camelProcessorDefinition.withId(step)

    }

    // parsing of the pollEnrich keyword
    case StepInformation(step @ PollEnrichDefinition(CamelSink(resourceUri), aggregationRef, timeout), camelProcessorDefinition: ProcessorDefinition[_]) => {

      val camelPollEnrichDefinition = aggregationRef match {
        case Left(ref) =>
          val definition = new CamelPollEnrichDefinition()
          definition.setResourceUri(resourceUri)
          definition.setAggregationStrategyRef(ref)
          definition.setTimeout(timeout)
          definition
        case Right(strategy) =>
          new CamelPollEnrichDefinition(strategy, resourceUri, timeout)
      }

      camelProcessorDefinition.addOutput(camelPollEnrichDefinition)
      camelProcessorDefinition.withId(step)
    }
  }

  protected implicit def enrichDSLExtension[I: ClassTag](baseDsl: BaseDSL[I]) = new EnricherDSL(baseDsl)

}
