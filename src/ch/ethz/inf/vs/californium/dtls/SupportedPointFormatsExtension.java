package ch.ethz.inf.vs.californium.dtls;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.californium.dtls.HelloExtensions.ExtensionType;
import ch.ethz.inf.vs.californium.util.DatagramReader;
import ch.ethz.inf.vs.californium.util.DatagramWriter;

/**
 * See <a href="http://tools.ietf.org/html/rfc4492#section-5.1.2">RFC 4492,
 * 5.1.2. Supported Point Formats Extension</a>.
 * 
 * @author Stefan Jucker
 * 
 */
public class SupportedPointFormatsExtension extends HelloExtension {

	// DTLS-specific constants ////////////////////////////////////////
	
	private static final int LENGTH_BITS = 16;
	
	private static final int LIST_LENGTH_BITS = 8;

	private static final int POINT_FORMAT_BITS = 8;

	// Members ////////////////////////////////////////////////////////

	/**
	 * Items in here are ordered according to the client's preferences (favorite
	 * choice first).
	 */
	List<ECPointFormat> ecPointFormatList;

	// Constructors ///////////////////////////////////////////////////

	public SupportedPointFormatsExtension(List<ECPointFormat> ecPointFormatList) {
		super(ExtensionType.EC_POINT_FORMATS);
		this.ecPointFormatList = ecPointFormatList;
	}
	
	// Methods ////////////////////////////////////////////////////////
	
	public void addECPointFormat(ECPointFormat format) {
		ecPointFormatList.add(format);
	}

	@Override
	public int getLength() {
		// fixed: type (2 bytes), length (2 bytes), list length (1 byte)
		// variable: number of point formats
		return 5 + ecPointFormatList.size();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append("\t\t\t\tType: " + type.toString() + " (" + type.getId() + ")\n");
		sb.append("\t\t\t\tLength: " + (getLength() - 4) + "\n");
		sb.append("\t\t\t\tEC point formats length: " + (getLength() - 5) + "\n");
		sb.append("\t\t\t\tElliptic Curves Point Formats (" + ecPointFormatList.size() + "):\n");

		for (ECPointFormat format : ecPointFormatList) {
			sb.append("\t\t\t\t\tEC point format: " + format.toString() + "\n");
		}

		return sb.toString();
	}
	
	// Serialization //////////////////////////////////////////////////
	
	@Override
	public byte[] toByteArray() {
		DatagramWriter writer = new DatagramWriter();
		writer.writeBytes(super.toByteArray());

		int listLength = ecPointFormatList.size();
		// list length + list length field (1 byte)
		writer.write(listLength + 1, LENGTH_BITS);
		writer.write(listLength, LIST_LENGTH_BITS);

		for (ECPointFormat format : ecPointFormatList) {
			writer.write(format.getId(), POINT_FORMAT_BITS);
		}

		return writer.toByteArray();
	}

	public static HelloExtension fromByteArray(byte[] byteArray) {
		DatagramReader reader = new DatagramReader(byteArray);

		int listLength = reader.read(LIST_LENGTH_BITS);

		List<ECPointFormat> ecPointFormatList = new ArrayList<ECPointFormat>();
		while (listLength > 0) {
			ECPointFormat format = ECPointFormat.getECPointFormatById(reader.read(POINT_FORMAT_BITS));
			ecPointFormatList.add(format);
			
			// one point format uses 1 byte
			listLength -= 1;
		}

		return new SupportedPointFormatsExtension(ecPointFormatList);
	}
	
	///////////////////////////////////////////////////////////////////

	/**
	 * See <a href="http://tools.ietf.org/html/rfc4492#section-5.1.2">RFC 4492,
	 * 5.1.2. Supported Point Formats Extension</a>.
	 * 
	 * @author Stefan Jucker
	 * 
	 */
	public enum ECPointFormat {
		UNCOMPRESSED(0), ANSIX962_COMPRESSED_PRIME(1), ANSIX962_COMPRESSED_CHAR2(2);

		private int id;

		private ECPointFormat(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		@Override
		public String toString() {
			switch (id) {
			case 0:
				return "uncompressed (" + id + ")";
			case 1:
				return "ansiX962_compressed_prime (" + id + ")";
			case 2:
				return "ansiX962_compressed_char2 (" + id + ")";
			default:
				return "";
			}
		}
		
		public static ECPointFormat getECPointFormatById(int id) {
			switch (id) {
			case 0:
				return ECPointFormat.UNCOMPRESSED;
			case 1:
				return ECPointFormat.ANSIX962_COMPRESSED_PRIME;
			case 2:
				return ECPointFormat.ANSIX962_COMPRESSED_CHAR2;

			default:
				return null;
			}
		}

	}

}
