package com.qvc.selenium.data;

public interface TestDataLoad<A, V>  {

	V getData(final A Type) throws InterruptedException, Exception;
}
