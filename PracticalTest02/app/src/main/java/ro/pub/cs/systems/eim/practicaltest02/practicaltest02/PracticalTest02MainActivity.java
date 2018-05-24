package ro.pub.cs.systems.eim.practicaltest02.practicaltest02;

import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PracticalTest02MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);

        Button send = findViewById(R.id.button);
        Button start = findViewById(R.id.button2);

        send.setOnClickListener(_clicker);
        start.setOnClickListener(_clicker);
    }

    private OnClick _clicker = new OnClick();
    private ServerThread _serverThread = null;

    private class OnClick implements Button.OnClickListener
    {

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.button:
                    break;
                case R.id.button2:
                    EditText port = findViewById(R.id.editText3);
                    int nrPort = 0;
                    Toast.makeText(getApplicationContext(), port.getText(), Toast.LENGTH_SHORT);
                    if (port.getText() == null || String.valueOf(port.getText()).equals("")) {
                        Log.e("server", "lipsa port");
                        break;
                    }
                    nrPort = Integer.parseInt(String.valueOf(port.getText()));
                    if (_serverThread != null)
                        _serverThread.stopServer();
                    _serverThread = new ServerThread(nrPort);
                    _serverThread.startServer();

                    break;
            }
        }
    }

    private class CommunicationThread extends Thread {
        private Socket socket;

        public CommunicationThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
//                Log.v(SyncStateContract.Constants.TAG, "Connection opened with "+socket.getInetAddress()+":"+socket.getLocalPort());
                Log.i("server", "Parse");
                BufferedReader reader = Utilities.getReader(socket);
                PrintWriter printWriter = Utilities.getWriter(socket);
                String[] cmds= reader.readLine().split(",");

                int a = Integer.parseInt(cmds[1]);
                int b = Integer.parseInt(cmds[2]);
                switch (cmds[0])
                {
                    case "add":
                    {
                        printWriter.write(String.valueOf(a + b));
                        break;
                    }
                    case "mul":
                    {
                        Thread.sleep(2000);
                        printWriter.write(String.valueOf(a * b));
                        break;
                    }
                }
                printWriter.flush();

                Log.i("server", cmds[0] + cmds[1] + cmds[2]);


//                printWriter.println(serverTextEditText.getText().toString());
                socket.close();
            } catch (IOException ioException) {
                Log.e("server", ioException.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class ServerThread extends Thread {
        public ServerThread(int port)
        {
            _port = port;
        }

        private boolean isRunning;

        private ServerSocket serverSocket;

        private int _port = 0;

        public void startServer() {
            isRunning = true;
            start();
        }

        public void stopServer() {
            isRunning = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (serverSocket != null) {
                            serverSocket.close();
                        }
                    } catch(IOException ioException) {
                        Toast.makeText(getApplicationContext(), ioException.getMessage(), Toast.LENGTH_SHORT);
                    }
                }
            }).start();
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(_port);
                while (isRunning) {
                    Log.i("server", "start server" + _port);
                    Socket socket = serverSocket.accept();
                    new CommunicationThread(socket).start();
                }
            } catch (IOException ioException) {
                Log.e("server", ioException.getMessage());
            }
        }
    }
}

class Utilities {

    public static BufferedReader getReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static PrintWriter getWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }

}
