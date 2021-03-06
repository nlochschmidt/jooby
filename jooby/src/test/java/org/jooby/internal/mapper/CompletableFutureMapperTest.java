package org.jooby.internal.mapper;

import static org.easymock.EasyMock.expect;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import org.jooby.Deferred;
import org.jooby.Deferred.Initializer0;
import org.jooby.test.MockUnit;
import org.jooby.test.MockUnit.Block;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CompletableFutureMapper.class, Deferred.class })
public class CompletableFutureMapperTest {

  private Block deferred = unit -> {
    Deferred deferred = unit.constructor(Deferred.class)
        .args(Deferred.Initializer0.class)
        .build(unit.capture(Deferred.Initializer0.class));
    unit.registerMock(Deferred.class, deferred);
  };

  @SuppressWarnings({"unchecked", "rawtypes" })
  private Block future = unit -> {
    CompletableFuture future = unit.get(CompletableFuture.class);
    expect(future.whenComplete(unit.capture(BiConsumer.class))).andReturn(future);
  };

  private Block init0 = unit -> {
    Initializer0 next = unit.captured(Deferred.Initializer0.class).iterator().next();
    next.run(unit.get(Deferred.class));
  };

  @SuppressWarnings({"rawtypes", "unchecked" })
  @Test
  public void resolve() throws Exception {
    Object value = new Object();
    new MockUnit(CompletableFuture.class)
        .expect(deferred)
        .expect(future)
        .expect(unit -> {
          Deferred deferred = unit.get(Deferred.class);
          deferred.resolve(value);
        })
        .run(unit -> {
          new CompletableFutureMapper()
              .map(unit.get(CompletableFuture.class));
        }, init0, unit -> {
          BiConsumer next = unit.captured(BiConsumer.class).iterator().next();
          next.accept(value, null);
        });
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  @Test
  public void reject() throws Exception {
    Throwable value = new Throwable();
    new MockUnit(CompletableFuture.class)
        .expect(deferred)
        .expect(future)
        .expect(unit -> {
          Deferred deferred = unit.get(Deferred.class);
          deferred.reject(value);
        })
        .run(unit -> {
          new CompletableFutureMapper()
              .map(unit.get(CompletableFuture.class));
        }, init0, unit -> {
          BiConsumer next = unit.captured(BiConsumer.class).iterator().next();
          next.accept(null, value);
        });
  }
}
