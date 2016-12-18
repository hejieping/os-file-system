package fileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class fileManager
{
	private char[][] disk; // ���̴洢��Ϊ128*256��char����
	private char[] FAT; // FAT���Ϊ1*128��char����
	private char[][] catalogue; // Ŀ¼��Ϊ256*32��char���飬Ŀ¼�����ļ����ļ��������ļ�����
	private char[] nowCata; // Ŀǰ��Ŀ¼��
	public final char FILETYPE = 'f'; // ��ʾ����Ϊ�ļ�
	public final char CATATYPE = 'c'; // ��ʾ����Ϊ�ļ���
	public final char DISKTYPE = 'd';// ��ʾ������Ϊ����
	public final char NULL = (char) 0; // ��ʾ���ַ�Ϊ���ա�
	public final int TYPEPOSITION = 16; // �����ֱ���Ŀ¼���д洢������Ϣ������λ��
	public final int SIZEPOSITION = 17; // ��ʾĿ¼���д洢��С��Ϣ������λ��
	// ��Ϊ�ļ�����λ�ô洢�Ĵ�С��Ϣ��ʾ�ļ�������ռ�е��̿�������Ϊ�ļ��У���ʾ���ļ����ж��ٸ�Ŀ¼�
	public final int FIRSTBLOCK = 18; // ��Ϊ�ļ����ͣ������ֱ�ʾ�ļ��������̿���disk��λ�ã�
	// ��Ϊ�ļ������ͣ���ʾ�ļ����µĵ�һ��Ŀ¼����catalogue��λ��
	public final int PARENTCATA = 19; // ��ʾ��Ŀ¼��ĸ�Ŀ¼��catalogue�����λ��
	public final int NEXTCATA = 20; // ��ʾͬһ��Ŀ¼�µ��ڽ�Ŀ¼������λ��
	private int NOWCATAPOSITION; // ��ʾĿǰ����Ŀ¼����catalogue�����λ��
	public final int FINALBLOCK = 127; // βĿ¼���ʶ
	public final int FINALCATA = 127; // β�̿��ʶ
	public final int DATASIZE = 256; // ���̿��С
	public final int CATASIZE = 32; // Ŀ¼�����ݽṹ��С
	public final int CATANAMESIZE = 16;// ����ļ�������
	public String path; // �ļ�·��
	private final String dataPath = "dataFile.hjp"; // ���ݱ���·��

	public fileManager() // ���캯��
	{
		disk = new char[FINALBLOCK][DATASIZE];
		FAT = new char[FINALBLOCK];
		catalogue = new char[FINALCATA][CATASIZE];
		if (!readLocalData())
		{
			arrayInit();
		}

	}

	private void deleteCatalogue(int position) // �����ṩ��Ŀ¼��λ��ɾ���ļ���
	{
		// ����ɾ����Ŀ¼���µ�������Ŀ¼����õĵݹ�˼��

		if (catalogue[position][SIZEPOSITION] == NULL)
		{
			// ������ļ���û���ļ�����ֱ��ɾ������
			deleteArray(position, CATASIZE);
			return;
		}
		int temp = (int) catalogue[position][FIRSTBLOCK];
		while (catalogue[temp][NEXTCATA] != FINALCATA) // ����Ŀ¼������һ��Ŀ¼��ʱ
		{
			int nextPositon = catalogue[temp][NEXTCATA];// �洢��һ��Ҫɾ����Ŀ¼���λ����Ϣ
			if (catalogue[temp][TYPEPOSITION] == CATATYPE) // ��Ҫɾ����Ŀ¼������Ϊ�ļ���
			{
				deleteCatalogue(temp);
			} else
			// ��Ҫɾ����Ŀ¼������Ϊ�ļ�
			{
				deleteFile(temp);
			}

			temp = nextPositon;

		}
		// ɾ�����һ����Ŀ¼��
		if (catalogue[temp][TYPEPOSITION] == CATATYPE)
		{
			deleteCatalogue(temp);
		} else
		{
			deleteFile(temp);
		}
		// ɾ������
		deleteArray(position, CATASIZE);

	}

	private int malloc(int type)// ����type���ͷ�����Ӧ�Ŀ�����
	{

		if (type == CATATYPE)
		{
			int randNum = (int) (Math.random() * FINALCATA);
			while (catalogue[randNum][0] != NULL)// �ж����������λ���Ƿ�Ϊ��Ŀ¼��
			{
				randNum = (int) (Math.random() * FINALCATA);
			}
			return randNum;
		} else
		{
			int randNum = (int) (Math.random() * FINALBLOCK);
			while (FAT[randNum] != NULL)// �ж����������λ���Ƿ�Ϊ��Ŀ¼��
			{
				randNum = (int) (Math.random() * FINALBLOCK);
			}
			return randNum;
		}
	}

	private void deleteDiskData(int position)// ��ָ��λ�õ��̿����
	{
		for (int i = 0; i < DATASIZE; i++)
		{
			disk[position][i] = NULL;
		}
	}

	private void deleteFile(int Cataposition)// ͨ��ָ���ļ����ڵ�Ŀ¼����λ����ɾ���ļ���disk������
	{
		// ��ȡ�ļ���disk�����̿��ַ
		int diskPosition = catalogue[Cataposition][FIRSTBLOCK];
		int temp;
		if (diskPosition == FINALBLOCK)
		{
			return;
		}
		while (FAT[diskPosition] != FINALBLOCK) // ͨ��������һɾ���ļ����̿������
		{
			deleteDiskData(diskPosition);
			temp = FAT[diskPosition];
			FAT[diskPosition] = NULL; // ��FAT��¼���̿���Ϣ�ÿ�
			diskPosition = temp;
		}
		deleteDiskData(diskPosition);
		FAT[diskPosition] = NULL;
	}

	private void arrayInit() // ���û�б������ݣ�����øú�����ʼ������
	{
		for (int i = 0; i < FINALBLOCK; i++)
		{
			FAT[i] = (char) 0;
			for (int j = 0; j < DATASIZE; j++)
			{
				disk[i][j] = (char) 0;
			}
		}
		for (int i = 0; i < FINALCATA; i++)
		{
			for (int j = 0; j < CATASIZE; j++)
			{
				catalogue[i][j] = (char) 0;
			}
		}
		// ��Ŀ¼��ʼ��
		catalogue[0][0] = 'r';
		catalogue[0][1] = 'o';
		catalogue[0][2] = 'o';
		catalogue[0][3] = 't';
		catalogue[0][TYPEPOSITION] = 'f';
		catalogue[0][FIRSTBLOCK] = (char) FINALCATA;
		nowCata = catalogue[0];

		NOWCATAPOSITION = 0;
	}

	private void deleteArray(int position, int length)// ���ָ������������
	{
		if (length == 32)
		{
			for (int i = 0; i < length; i++)
			{
				catalogue[position][i] = NULL;
			}
		}
		if (length == 256)
		{
			for (int i = 0; i < length; i++)
			{
				disk[position][i] = NULL;
			}
		}
	}

	private boolean isSameName(int position, String name) // �ж�ָ��λ�õ��ļ����Ƿ��name��ͬ
	{
		int i;
		for (i = 0; i < name.length(); i++)
		{
			if (catalogue[position][i] != name.charAt(i))
			{
				return false;
			}
		}
		if (catalogue[position][i] == NULL)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public FilePosition findCataPosition(String cataName, int type) // Ѱ�ҵ�ǰĿ¼����ļ�λ��
	{
		if (cataName == null
				|| catalogue[NOWCATAPOSITION][FIRSTBLOCK] == FINALCATA
				|| cataName.length() == 0)
		{
			FilePosition position = new FilePosition(NULL, NULL);
			return position;
		}
		int preCataPositon = NOWCATAPOSITION;// ��¼��������Ŀ¼���ǰһĿ¼���λ��
		int nowPosition = catalogue[preCataPositon][FIRSTBLOCK]; // ��¼����������Ŀ¼���λ��
		// ���ļ������ļ��������ʱ���ж��ҵ����ļ�
		while (!(isSameName(nowPosition, cataName) && (int) catalogue[nowPosition][TYPEPOSITION] == type)
				&& catalogue[nowPosition][NEXTCATA] != FINALCATA) // ѭ��ֱ���ҵ�Ҫɾ����Ŀ¼��
		{
			preCataPositon = nowPosition;
			nowPosition = catalogue[nowPosition][NEXTCATA];
		}
		// �ҵ��ļ�
		if ((isSameName(nowPosition, cataName) && (int) catalogue[nowPosition][TYPEPOSITION] == type))
		{
			FilePosition position = new FilePosition(preCataPositon,
					nowPosition);
			return position;

		} else
		// �Ҳ����ļ�
		{
			FilePosition position = new FilePosition(NULL, NULL);
			return position;
		}

	}

	public boolean fileExist(String cataName, int type) // �ж��ļ��Ƿ����
	{
		FilePosition filePosition = findCataPosition(cataName, type);
		if (filePosition.getNowPosition() == NULL
				&& filePosition.getPrePosition() == NULL)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public void createCatalogue(String cataString) // �½��ļ���
	{
		// ������Ŀ¼���������λ�������������
		int randNum = malloc(CATATYPE);
		// ��catalogue��Ŀ¼��洢��Ӧ����Ϣ
		for (int i = 0; i < cataString.length(); i++)
		{
			catalogue[randNum][i] = cataString.charAt(i);
		}
		catalogue[randNum][FIRSTBLOCK] = (char) FINALCATA;
		catalogue[randNum][TYPEPOSITION] = CATATYPE;
		catalogue[randNum][PARENTCATA] = (char) NOWCATAPOSITION; // Ŀ¼��ĸ�Ŀ¼Ϊ��ǰĿ¼��λ��
		catalogue[randNum][NEXTCATA] = (char) catalogue[NOWCATAPOSITION][FIRSTBLOCK]; // ��Ŀ¼��Ϊ��Ŀ¼�µ��ڽ�Ŀ¼��ĵ�һ��
		catalogue[NOWCATAPOSITION][FIRSTBLOCK] = (char) randNum; // ����Ŀ¼����׸���Ŀ¼��λ�ü�¼Ϊ�²����Ŀ¼���λ��
		catalogue[NOWCATAPOSITION][SIZEPOSITION]++;

	}

	public void deleteCatalogue(String deleteCataName) // ɾ���ļ��У����õݹ�
	{
		FilePosition position = findCataPosition(deleteCataName, CATATYPE);
		if (position.getPrePosition() == NOWCATAPOSITION)
		{
			// ��ǰĿ¼���µļ�¼�ĵ�һ����Ŀ¼��λ���޸�Ϊ��һ����Ŀ¼����ڽ�Ŀ¼���λ��
			catalogue[NOWCATAPOSITION][FIRSTBLOCK] = catalogue[position
					.getNowPosition()][NEXTCATA];
			deleteCatalogue(position.getNowPosition()); // ɾ���ҵ�����Ŀ¼��
		} else
		{
			// Ҫɾ����Ŀ¼���ǰһ��ͺ�һ��������
			catalogue[position.getPrePosition()][NEXTCATA] = catalogue[position
					.getNowPosition()][NEXTCATA];
			// ɾ���ҵ�����Ŀ¼��
			deleteCatalogue(position.getNowPosition());
		}

	}

	// �ж�position��ָʾ��Ŀ¼�������Ƿ��nameһ��

	public void createFile(String fileName) // �½��ļ�
	{
		// ������Ŀ¼���������λ�������������
		int randNum = malloc(CATATYPE);
		// ��catalogue��Ŀ¼��洢��Ӧ����Ϣ
		for (int i = 0; i < fileName.length(); i++)
		{
			catalogue[randNum][i] = fileName.charAt(i);
		}
		catalogue[randNum][FIRSTBLOCK] = (char) FINALBLOCK;
		catalogue[randNum][TYPEPOSITION] = FILETYPE;
		catalogue[randNum][PARENTCATA] = (char) NOWCATAPOSITION; // Ŀ¼��ĸ�Ŀ¼Ϊ��ǰĿ¼��λ��
		catalogue[randNum][NEXTCATA] = (char) catalogue[NOWCATAPOSITION][FIRSTBLOCK]; // ��Ŀ¼��Ϊ��Ŀ¼�µ��ڽ�Ŀ¼��ĵ�һ��
		catalogue[NOWCATAPOSITION][FIRSTBLOCK] = (char) randNum; // ����Ŀ¼����׸���Ŀ¼��λ�ü�¼Ϊ�²����Ŀ¼���λ��
		catalogue[NOWCATAPOSITION][SIZEPOSITION]++; // ��Ŀ¼���ļ���С+1
	}

	public String readFile(String fileName)// ��ȡ�ļ��洢��disk������
	{
		String dataString = new String();
		// �����ļ�λ��
		int CataPosition = findCataPosition(fileName, FILETYPE)
				.getNowPosition();
		int diskPosition = catalogue[CataPosition][FIRSTBLOCK];
		if (diskPosition == FINALBLOCK) // ���ļ�û�����ݣ����ؿ��ַ���
		{
			String temp = "";
			return temp;
		}
		int a = FAT[diskPosition];
		while ((int) FAT[diskPosition] != FINALBLOCK) // ��fat���¼������Ϊ���һ�飬ֹͣѭ��
		{
			dataString += String.valueOf(disk[diskPosition]);
			diskPosition = FAT[diskPosition];
		}
		dataString += String.valueOf(disk[diskPosition]);
		return dataString;
	}

	public void writeFile(String fileName, String data) // ���ļ�д����
	{
		// Ѱ���ļ�λ��
		FilePosition filePosition = findCataPosition(fileName, FILETYPE);
		// ɾ��ԭ�е��ļ�����
		deleteFile(filePosition.getNowPosition());
		// �����̿����
		int size = (data.length() - 1) / FINALBLOCK + 1;
		int length = 0; // �Ѿ�������ַ�����
		int diskPosition = malloc(DISKTYPE);// ������̿�
		FAT[diskPosition] = (char) FINALBLOCK; // �����̿��޸�Ϊ��ռ��
		catalogue[filePosition.getNowPosition()][FIRSTBLOCK] = (char) diskPosition;// �����ļ����̿��ַ
		for (int i = 0; i < size - 1; i++)
		{

			for (int j = 0; j < DATASIZE; j++)
			{
				disk[diskPosition][i] = data.charAt(length);
				length++;
			}
			int temp = malloc(DISKTYPE);// ��ȡ��һ�̿�ĵ�ַ
			FAT[diskPosition] = (char) temp;// �������̿����һ��ַ��Ϣ
			FAT[temp] = (char) FINALBLOCK;// �����̿��޸�Ϊ��ռ��
			diskPosition = temp;

		}
		// ��¼���һ���̿����Ϣ
		for (int i = 0; length < data.length(); length++, i++)
		{
			disk[diskPosition][i] = data.charAt(length);
		}

	}

	public void deleteFile(String fileName)// �����ļ�����ɾ����ǰĿ¼�µ��ļ�
	{
		FilePosition position = findCataPosition(fileName, FILETYPE);
		if (position.getPrePosition() == NOWCATAPOSITION)
		{
			// ��ǰĿ¼���µļ�¼�ĵ�һ����Ŀ¼��λ���޸�Ϊ��һ����Ŀ¼����ڽ�Ŀ¼���λ��
			catalogue[NOWCATAPOSITION][FIRSTBLOCK] = catalogue[position
					.getNowPosition()][NEXTCATA];
			deleteFile(position.getNowPosition()); // ɾ���ҵ�����Ŀ¼��
		} else
		{
			// Ҫɾ����Ŀ¼���ǰһ��ͺ�һ��������
			catalogue[position.getPrePosition()][NEXTCATA] = catalogue[position
					.getNowPosition()][NEXTCATA];
			// ɾ���ҵ�����Ŀ¼��
			deleteFile(position.getNowPosition());
		}
	}

	public void enterCata(String cataName)// ������Ŀ¼
	{
		FilePosition filePosition = findCataPosition(cataName, CATATYPE);
		NOWCATAPOSITION = filePosition.getNowPosition();
		nowCata = catalogue[NOWCATAPOSITION];
		path = path + "\\" + cataName;
	}

	public void parentCata()// �����ϼ�Ŀ¼
	{
		NOWCATAPOSITION = catalogue[NOWCATAPOSITION][PARENTCATA];
		nowCata = catalogue[NOWCATAPOSITION];
	}

	public int getSonCataSize(int position) // ��ȡ���ļ��µ����ļ�����
	{
		int size = 0;
		position = (int) catalogue[position][FIRSTBLOCK];
		while (position != FINALCATA)
		{
			size++;
			position = catalogue[position][NEXTCATA];
		}
		return size;

	}

	public int getNowCP() // ��ȡ����Ŀ¼λ��
	{
		return NOWCATAPOSITION;
	}

	public String getCataName(int position)// ��ȡĿ¼������
	{
		String name = new String();
		int i = 0;
		while (catalogue[position][i] != NULL && i < CATANAMESIZE)
		{
			name += catalogue[position][i];
			i++;
		}
		return name;
	}

	public void pathUpdate() // Ŀ¼·������
	{
		ArrayList<String> cataName = new ArrayList<String>();
		int position = NOWCATAPOSITION;
		while (position != NULL)
		{
			cataName.add(getCataName(position));
			position = catalogue[position][PARENTCATA];
		}
		cataName.add(getCataName(position));
		String cataPath = new String();
		for (int i = 0; i < cataName.size() - 1; i++)
		{
			cataPath += cataName.get(cataName.size() - i - 1);
			cataPath += "\\";
		}
		cataPath += cataName.get(0);

		path = cataPath;

	}

	public int getNextCP(int position) // ��ȡ��һ���ļ���ַ
	{
		return (int) catalogue[position][NEXTCATA];
	}

	public int getSonCata(int position)
	{
		return catalogue[position][FIRSTBLOCK];
	}

	public char getType(int position)
	{
		return catalogue[position][TYPEPOSITION];
	}

	private boolean readLocalData() // ��ȡ�������ݣ����ݲ������򷵻�false��
	{
		// ��ȡ�洢���ݵ��ļ�
		File dataFile = new File(dataPath);
		if ((dataFile.exists()) && (dataFile.isFile())) // �ļ����ڣ���ȡ����
		{
			try
			{
				FileInputStream fileInputStream = new FileInputStream(dataFile);
				if (fileInputStream.available() == 0) // �ж��ļ��Ƿ�Ϊ��
				{
					fileInputStream.close();
					return false;
				}
			} catch (FileNotFoundException e1)
			{
				// TODO �Զ����ɵ� catch ��
				e1.printStackTrace();
			} catch (IOException e)
			{
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}

			try
			{
				FileReader reader = new FileReader(dataFile);
				for (int i = 0; i < FINALBLOCK; i++)
				{
					reader.read(disk[i]);
				}
				reader.read(FAT);
				for (int i = 0; i < FINALCATA; i++)
				{
					reader.read(catalogue[i]);
				}
				reader.close();
				return true;
			} catch (FileNotFoundException e)
			{
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
				return false;
			} catch (IOException e)
			{
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
				return false;
			}

		} else
		// �ļ������ڣ��������ļ�
		{
			try
			{
				dataFile.createNewFile();
				return false;
			} catch (IOException e)
			{
				return false;
			}
		}

	}

	public void writeData2Local()// �����ݱ��浽����
	{
		File dataFile = new File(dataPath);
		if (!dataFile.exists()) // �ļ��������򴴽�һ���µ��ļ�
		{
			try
			{
				dataFile.createNewFile();
			} catch (IOException e)
			{
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
		try
		{
			FileWriter writer = new FileWriter(dataFile);
			for (int i = 0; i < FINALBLOCK; i++)
			{
				writer.write(disk[i]);
			}

			writer.write(FAT);
			for (int i = 0; i < FINALCATA; i++)
			{
				writer.write(catalogue[i]);
			}
			writer.close();
		} catch (IOException e)
		{
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}

	}

}
