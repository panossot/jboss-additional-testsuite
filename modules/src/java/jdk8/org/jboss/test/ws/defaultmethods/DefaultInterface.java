package org.jboss.test.ws.defaultmethods;

public interface DefaultInterface
{
   default public String sayHi() {
      return "Hi, Default";
   }
}
