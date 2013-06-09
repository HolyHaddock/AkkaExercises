package com.akkaexercises

package object examples {
  def someLongBlockingDBOp(i: Integer) = {
    Thread.sleep(1000); 
    (i, i*5); 
  }
} 