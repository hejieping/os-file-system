package fileManager;

public class FilePosition
{
	private int prePosition; // ��һ�ļ�λ��
	private int nowPosition; // ��ǰ�ļ�λ��

	public FilePosition()
	{
		prePosition = 0;
		nowPosition = 0;
	}

	public FilePosition(int pre, int now)
	{
		prePosition = pre;
		nowPosition = now;
	}

	public void setPosition(int pre, int now)
	{
		prePosition = pre;
		nowPosition = now;
	}

	public int getPrePosition()
	{
		return prePosition;
	}

	public int getNowPosition()
	{
		return nowPosition;
	}

}
