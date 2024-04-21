package middleware;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ui.ISceneManager;


class ControllerTest {
    @Test
    void testServerSuccess() {
        ServerCommunicatorMock serverCommunicatorMock = new ServerCommunicatorMock();
        SceneManagerMock sceneManagerMock = new SceneManagerMock();
        serverCommunicatorMock.setThrowError(false);
        Controller controller = new Controller(serverCommunicatorMock);
        controller.setSceneManager(sceneManagerMock);
        assertFalse(sceneManagerMock.getErrorSceneShown());
        assertEquals("response", controller.sendRequestWithCheck("/status", null, "GET"));
        assertFalse(sceneManagerMock.getErrorSceneShown());
    }

    @Test
    void testServerFailure() {
        ServerCommunicatorMock serverCommunicatorMock = new ServerCommunicatorMock();
        SceneManagerMock sceneManagerMock = new SceneManagerMock();
        serverCommunicatorMock.setThrowError(true);
        Controller controller = new Controller(serverCommunicatorMock);
        controller.setSceneManager(sceneManagerMock);
        assertTrue(sceneManagerMock.getErrorSceneShown());
        assertEquals(null, controller.sendRequestWithCheck("/status", null, "GET"));
        assertTrue(sceneManagerMock.getErrorSceneShown());
    }
}

/*
 * Mocks ServerCommunicator to test server error message
 */
class ServerCommunicatorMock extends ServerCommunicator {

    private boolean throwError;

    public void setThrowError(boolean throwError) {
        this.throwError = throwError;
    }

    @Override
    public String sendRequest(String path, String query, String method) throws Exception {
        if (throwError) {
            throw new Exception();
        } else {
            return "response";
        }
    }
}


/*
 * Mocks SceneManager to confirm error scene is shown
 */
class SceneManagerMock implements ISceneManager {

    private boolean errorSceneShown = false;
    
    public SceneManagerMock() {
    }

    public boolean getErrorSceneShown() {
        return errorSceneShown;
    }

    public void displayServerErrorScene() {
        errorSceneShown = true;
    }
}