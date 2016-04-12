package ca.sbstn.androidb.util;

import android.content.Context;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {
	@Mock
	Context mContext;

	@Test
	public void testDpToPixels() {
		Assert.assertEquals("expected pixel result different from actual", 1.0, 9.0, 0);
	}
}