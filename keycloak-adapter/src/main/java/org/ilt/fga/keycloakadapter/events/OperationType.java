package org.ilt.fga.keycloakadapter.events;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum OperationType {
  CREATE(0),
  UPDATE(1),
  DELETE(2),
  ACTION(3);

  private final int stableIndex;

  private static final Map<Integer, OperationType> BY_ID =
      Stream.of(values())
          .collect(Collectors.toMap(OperationType::getStableIndex, Function.identity()));

  private OperationType(int index) {
    this.stableIndex = index;
  }

  public int getStableIndex() {
    return stableIndex;
  }

  public static OperationType valueOfInteger(Integer id) {
    return id == null ? null : BY_ID.get(id);
  }
}
