package de.caritas.cob.consultingtypeservice.api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.io.PrintWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class LogServiceTest {

  @Mock Exception exception;

  @Mock private Logger logger;

  @BeforeEach
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void logWarning_Should_LogExceptionStackTrace() {

    LogService.logWarning(exception);
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }
}
