package co.com.crediya.r2dbc.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgreSQLConnectionPoolTest {

    @InjectMocks
    private PostgreSQLConnectionPool connectionPool;

    @Mock
    private PostgresqlConnectionProperties properties;


    @BeforeEach
    void setUp() {
        Mockito.when(this.properties.host()).thenReturn("localhost");
        Mockito.when(this.properties.port()).thenReturn(5432);
        Mockito.when(this.properties.database()).thenReturn("dbName");
        Mockito.when(this.properties.schema()).thenReturn("schema");
        Mockito.when(this.properties.username()).thenReturn("username");
        Mockito.when(this.properties.password()).thenReturn("password");
    }

    @Test
    void getConnectionConfigSuccess() {
        Assertions.assertNotNull(this.connectionPool.getConnectionConfig(this.properties));
    }
}
