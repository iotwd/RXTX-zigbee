package exception;

public class PortInUse extends Throwable {
    private static final long serialVersionUID = 1L;

    public PortInUse() {}

    @Override
    public String toString() {
        return "端口已被占用！打开串口操作失败！";
    }

}
