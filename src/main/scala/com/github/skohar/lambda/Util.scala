package com.github.skohar.lambda

import com.amazonaws.services.lambda.runtime.Context

object Util {
  implicit class RichContext(context: Context) {
    def logger = context.getLogger
  }
}
