package cn.hutool.core.lang;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Snowflake单元测试
 * @author Looly
 *
 */
public class SnowflakeTest {

	/**
	 * 测试-根据传入时间戳-计算ID起终点
	 */
	@Test
	public void snowflakeTestGetIdScope() {
		final long workerId = RandomUtil.randomLong(31);
		final long dataCenterId = RandomUtil.randomLong(31);
		final Snowflake idWorker = new Snowflake(workerId, dataCenterId);
		final long generatedId = idWorker.nextId();
		// 随机忽略数据中心和工作机器的占位
		final boolean ignore = RandomUtil.randomBoolean();
		final long createTimestamp = idWorker.getGenerateDateTime(generatedId);
		final Pair<Long, Long> idScope = idWorker.getIdScopeByTimestamp(createTimestamp, createTimestamp, ignore);
		final long startId = idScope.getKey();
		final long endId = idScope.getValue();

		// 起点终点相差比较
		final long trueOffSet = endId - startId;
		// 忽略数据中心和工作机器时差值为22个1，否则为12个1
		final long expectedOffSet = ignore ? ~(-1 << 22) : ~(-1 << 12);
		assertEquals(trueOffSet, expectedOffSet);
	}

	@Test
	public void snowflakeTest1(){
		//构建Snowflake，提供终端ID和数据中心ID
		final Snowflake idWorker = new Snowflake(0, 0);
		final long nextId = idWorker.nextId();
		assertTrue(nextId > 0);
	}

	@Test
	public void snowflakeTest(){
		final HashSet<Long> hashSet = new HashSet<>();

		//构建Snowflake，提供终端ID和数据中心ID
		final Snowflake idWorker = new Snowflake(0, 0);
		for (int i = 0; i < 1000; i++) {
			final long id = idWorker.nextId();
			hashSet.add(id);
		}
		assertEquals(1000L, hashSet.size());
	}

	@Test
	public void snowflakeGetTest(){
		//构建Snowflake，提供终端ID和数据中心ID
		final Snowflake idWorker = new Snowflake(1, 2);
		final long nextId = idWorker.nextId();

		assertEquals(1, idWorker.getWorkerId(nextId));
		assertEquals(2, idWorker.getDataCenterId(nextId));
		assertTrue(idWorker.getGenerateDateTime(nextId) - System.currentTimeMillis() < 10);
	}

	@Test
	@Disabled
	public void uniqueTest(){
		// 测试并发环境下生成ID是否重复
		final Snowflake snowflake = IdUtil.getSnowflake(0, 0);

		final Set<Long> ids = new ConcurrentHashSet<>();
		ThreadUtil.concurrencyTest(100, () -> {
			for (int i = 0; i < 50000; i++) {
				if(false == ids.add(snowflake.nextId())){
					throw new UtilException("重复ID！");
				}
			}
		});
	}

	@Test
	public void getSnowflakeLengthTest(){
		for (int i = 0; i < 1000; i++) {
			final long l = IdUtil.getSnowflake(0, 0).nextId();
			assertEquals(19, StrUtil.toString(l).length());
		}
	}

	@Test
	@Disabled
	public void snowflakeRandomSequenceTest(){
		final Snowflake snowflake = new Snowflake(null, 0, 0,
				false, Snowflake.DEFAULT_TIME_OFFSET, 2);
		for (int i = 0; i < 1000; i++) {
			final long id = snowflake.nextId();
			Console.log(id);
			ThreadUtil.sleep(10);
		}
	}

	@Test
	@Disabled
	public void uniqueOfRandomSequenceTest(){
		// 测试并发环境下生成ID是否重复
		final Snowflake snowflake = new Snowflake(null, 0, 0,
				false, Snowflake.DEFAULT_TIME_OFFSET, 100);

		final Set<Long> ids = new ConcurrentHashSet<>();
		ThreadUtil.concurrencyTest(100, () -> {
			for (int i = 0; i < 50000; i++) {
				if(false == ids.add(snowflake.nextId())){
					throw new UtilException("重复ID！");
				}
			}
		});
	}
}
