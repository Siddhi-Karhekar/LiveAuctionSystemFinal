package dao;

import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Sample DAO test using Mockito to stub the JDBC layer.
 * Demonstrates how to unit-test a DAO without a live MySQL instance.
 */
class UserDAOTest {

    private MockedStatic<DBConnection> dbMock;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dbMock = mockStatic(DBConnection.class);
        dbMock.when(DBConnection::getConnection).thenReturn(conn);
        // close() is a no-op for our mock
        dbMock.when(() -> DBConnection.close(any())).thenAnswer(inv -> null);
    }

    @AfterEach
    void tearDown() {
        dbMock.close();
    }

    @Test
    void register_returnsTrue_whenInsertAffectsOneRow() throws Exception {
        when(ps.executeUpdate()).thenReturn(1);

        User user = new User(0, "Alice", "alice@example.com", "secret", "buyer");
        UserDAO dao = new UserDAO();

        assertTrue(dao.register(user));
        verify(ps).setString(1, "Alice");
        verify(ps).setString(2, "alice@example.com");
        verify(ps).setString(4, "buyer");
        // password should be hashed (64 hex chars), not the plain "secret"
        verify(ps).setString(eq(3), argThat(s -> s.length() == 64 && !s.equals("secret")));
    }

    @Test
    void register_returnsFalse_whenInsertAffectsZeroRows() throws Exception {
        when(ps.executeUpdate()).thenReturn(0);

        UserDAO dao = new UserDAO();
        assertFalse(dao.register(new User(0, "Bob", "bob@example.com", "pw", "buyer")));
    }

    @Test
    void login_returnsUser_whenCredentialsMatch() throws Exception {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(42);
        when(rs.getString("name")).thenReturn("Alice");
        when(rs.getString("email")).thenReturn("alice@example.com");
        when(rs.getString("password")).thenReturn("hashed");
        when(rs.getString("role")).thenReturn("buyer");
        when(rs.getTimestamp("created_at")).thenReturn(new Timestamp(0));

        User result = new UserDAO().login("alice@example.com", "secret");

        assertNotNull(result);
        assertEquals(42, result.getId());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("buyer", result.getRole());
    }

    @Test
    void login_returnsNull_whenNoMatchingRow() throws Exception {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(new UserDAO().login("nobody@example.com", "wrong"));
    }

    @Test
    void findByEmail_returnsNull_whenUserDoesNotExist() throws Exception {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(new UserDAO().findByEmail("ghost@example.com"));
    }
}
