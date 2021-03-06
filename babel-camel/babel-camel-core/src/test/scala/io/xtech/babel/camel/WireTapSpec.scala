/*
 *
 *  Copyright 2010-2014 Crossing-Tech SA, EPFL QI-J, CH-1015 Lausanne, Switzerland.
 *  All rights reserved.
 *
 * ==================================================================================
 */

package io.xtech.babel.camel

import io.xtech.babel.camel.mock._
import io.xtech.babel.camel.test.camel
import org.apache.camel.builder.{ RouteBuilder => CRouteBuilder }
import org.specs2.mutable.SpecificationWithJUnit

class WireTapSpec extends SpecificationWithJUnit {
  sequential

  "create a wire-tap" in new camel {

    val testMessage = "test"
    val wireTapMessage = "tap"

    import io.xtech.babel.camel.builder.RouteBuilder

    val routeDef = new RouteBuilder {
      //#doc:babel-camel-wiretap
      from("direct:input-babel").
        //Incoming messages are sent to the direct endpoint
        //   and to the next mock endpoint
        wiretap("direct:babel-tap")
        .to("mock:output-babel")
      //#doc:babel-camel-wiretap

      from("direct:babel-tap").processBody(_ => {
        Thread.sleep(1000);
        "tap"
      }).to("mock:output-babel").to("mock:babel-tap")
    }

    val nativeRoute = new CRouteBuilder() {
      def configure(): Unit = {
        from("direct:input-camel").wireTap("direct:camel-tap")
          .to("mock:output-camel")

        from("direct:camel-tap").delay(1000).setBody(constant("tap")).to("mock:output-camel").to("mock:camel-tap")
      }
    }

    routeDef.addRoutesToCamelContext(camelContext)
    camelContext.addRoutes(nativeRoute)

    camelContext.start()

    val mockEndpointB = camelContext.mockEndpoint("output-babel")
    val mockEndpointBT = camelContext.mockEndpoint("babel-tap")
    val mockEndpointC = camelContext.mockEndpoint("output-camel")
    val mockEndpointCT = camelContext.mockEndpoint("camel-tap")

    mockEndpointB.expectedBodiesReceived(testMessage, wireTapMessage)
    mockEndpointBT.expectedBodiesReceived(wireTapMessage)
    mockEndpointC.expectedBodiesReceived(testMessage, wireTapMessage)
    mockEndpointCT.expectedBodiesReceived(wireTapMessage)

    val producer = camelContext.createProducerTemplate()

    producer.sendBody("direct:input-camel", testMessage)
    producer.sendBody("direct:input-babel", testMessage)

    mockEndpointC.assertIsSatisfied()
    mockEndpointCT.assertIsSatisfied()
    mockEndpointB.assertIsSatisfied()
    mockEndpointBT.assertIsSatisfied()

  }

  "create a functionnal wire-tap" in new camel {

    val testMessage = "test"
    val wireTapMessage = "tap"

    import io.xtech.babel.camel.builder.RouteBuilder

    val routeDef = new RouteBuilder {
      //#doc:babel-camel-wiretap-functional
      from("direct:input-babel").
        //Incoming messages are sent to the direct endpoint
        //   and to the next mock endpoint
        sideEffect(_.to("mock:in-wire").processBody(_ => wireTapMessage).to("mock:out-wire"))
        .to("mock:output")
      //#doc:babel-camel-wiretap-functional

    }

    routeDef.addRoutesToCamelContext(camelContext)

    camelContext.start()

    val mockEndpointBI = camelContext.mockEndpoint("in-wire")
    val mockEndpointBO = camelContext.mockEndpoint("out-wire")
    val mockEndpointBT = camelContext.mockEndpoint("output")

    mockEndpointBI.expectedBodiesReceived(testMessage)
    mockEndpointBO.expectedBodiesReceived(wireTapMessage)
    mockEndpointBT.expectedBodiesReceived(testMessage)

    val producer = camelContext.createProducerTemplate()

    producer.sendBody("direct:input-babel", testMessage)

    mockEndpointBI.assertIsSatisfied()
    mockEndpointBO.assertIsSatisfied()
    mockEndpointBT.assertIsSatisfied()

  }
}
