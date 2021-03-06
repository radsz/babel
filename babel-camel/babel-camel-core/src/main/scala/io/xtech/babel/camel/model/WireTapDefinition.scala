/*
 *
 *  Copyright 2010-2014 Crossing-Tech SA, EPFL QI-J, CH-1015 Lausanne, Switzerland.
 *  All rights reserved.
 *
 * ==================================================================================
 */

package io.xtech.babel.camel.model

import io.xtech.babel.fish.model.{ Sink, StepDefinition }
import org.apache.camel.processor.aggregate.AggregationStrategy

import scala.collection.immutable

/**
  * Defines a sink where each message is just copied.
  */
case class WireTapDefinition[T](sink: CamelSink[T]) extends StepDefinition
