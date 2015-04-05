/* $Id: ProtocolService.java,v 1.4 2008/05/24 22:25:52 linuxguy79 Exp $ */

package edu.crest.dlt.ibp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import edu.crest.dlt.exception.IBPException;
import edu.crest.dlt.transfer.TransferThread;
import edu.crest.dlt.utils.Configuration;

final class IBPCommand
{
	private static Logger log = Logger.getLogger(IBPCommand.class.getName());

	// Protocol version tag.
	static final int PROTOCOL_VERSION = 0;

	// Command tags.
	static final int ALLOCATE = 1;
	static final int STORE = 2;
	static final int DELIVER = 3;
	static final int STATUS = 4;
	static final int SEND = 5;
	static final int MCOPY = 6;
	static final int REMOTE_STORE = 7;
	static final int LOAD = 8;
	static final int MANAGE = 9;
	static final int NOP = -1;

	// MANAGE sub-command tags.
	static final int PROBE = 40;
	static final int INCREMENT = 41;
	static final int DECREMENT = 42;
	static final int CHANGE = 43;
	static final int CONFIG = 44;

	static final int STATUS_INQUIRE = 1;
	static final int STATUS_CHANGE = 2;

	// Reliability codes.
	static final int SOFT = 1;
	static final int HARD = 2;

	// Allocation type codes.
	static final int BYTEARRAY = 1;
	static final int BUFFER = 2;
	static final int FIFO = 3;
	static final int CIRQ = 4;

	// Success return code
	static final int SUCCESS = 1;

	// Generic allocate routine.
	private static final Allocation allocate(Socket socket_to_read, Depot depot, int duration,
			long size, int reliability, int type) throws IBPException
	{
		String ibp_server = depot != null ? "IBP [" + depot + "]" : "IBP [unknown]";
		TransferThread transfer_thread = (TransferThread) Thread.currentThread();
		int timeout = Configuration.dlt_depot_request_timeout;

		try {
			socket_to_read.setSoTimeout(timeout);

			OutputStream stream_out = socket_to_read.getOutputStream();
			// InputStream is = s.getInputStream();
			BufferedReader buffer_in = new BufferedReader(new InputStreamReader(
					socket_to_read.getInputStream()));

			String ibp_command = new String(PROTOCOL_VERSION + " " + ALLOCATE + " " + reliability + " "
					+ type + " " + duration + " " + size + " " + (timeout / 1000) + "\n");

			log.info(ibp_server + ": outgoing command [" + ibp_command + "]");
			transfer_thread.transfer_status = ibp_server + ": requesting new allocation [" + size
					+ " bytes; " + duration + " s; " + type + " tpye]";

			stream_out.write(ibp_command.getBytes());

			StringBuffer ibp_response = new StringBuffer();
			// int data=is.read();
			// while(data!=-1) {
			// buf.append((char)data);
			// data=is.read();
			// }
			ibp_response.append(buffer_in.readLine());

			log.info(ibp_server + ": response [" + ibp_response + "]");
			transfer_thread.transfer_status = ibp_server + ": response [" + ibp_response + "]";

			String[] ibp_response_fields = ibp_response.toString().split("[ \n]");

			if (ibp_response_fields.length < 3) {
				throw (new IBPException(Integer.parseInt(ibp_response_fields[1])));
			}

			log.info(ibp_server + ": creating capabilities");
			transfer_thread.transfer_status = ibp_server + ": creating capabilities";

			Capability capability_read = new Capability(Capability.CAPABILITY_READ,
					ibp_response_fields[1]);
			Capability capability_write = new Capability(Capability.CAPABILITY_WRITE,
					ibp_response_fields[2]);
			Capability capability_manage = new Capability(Capability.CAPABILITY_MANAGE,
					ibp_response_fields[3]);

			log.info(ibp_server + ": new allocation created");
			transfer_thread.transfer_status = ibp_server + ": new allocation created";

			return (new Allocation(capability_read, capability_write, capability_manage));
		} catch (Exception e) {
			throw (new IBPException(-38));
		}
	}

	static final Allocation allocateSoftByteArray(Socket socket_to_read, Depot depot, int duration,
			long size) throws IBPException
	{
		return (allocate(socket_to_read, depot, duration, size, SOFT, BYTEARRAY));
	}

	static final Allocation allocateHardByteArray(Socket socket_to_read, Depot depot, int duration,
			long size) throws IBPException
	{
		return (allocate(socket_to_read, depot, duration, size, HARD, BYTEARRAY));
	}

	static final Allocation allocateSoftBuffer(Socket socket_to_read, Depot depot, int duration,
			long size) throws IBPException
	{
		return (allocate(socket_to_read, depot, duration, size, SOFT, BUFFER));
	}

	static final Allocation allocateHardBuffer(Socket socket_to_read, Depot depot, int duration,
			long size) throws IBPException
	{
		return (allocate(socket_to_read, depot, duration, size, HARD, BUFFER));
	}

	static final Allocation allocateSoftFifo(Socket socket_to_read, Depot depot, int duration,
			long size) throws IBPException
	{
		return (allocate(socket_to_read, depot, duration, size, SOFT, FIFO));
	}

	static final Allocation allocateHardFifo(Socket socket_to_read, Depot depot, int duration,
			long size) throws IBPException
	{
		return (allocate(socket_to_read, depot, duration, size, HARD, FIFO));
	}

	static final Allocation allocateSoftCirq(Socket socket_to_read, Depot depot, int duration,
			long size) throws IBPException
	{
		return (allocate(socket_to_read, depot, duration, size, SOFT, CIRQ));
	}

	static final Allocation allocateHardCirq(Socket socket_to_read, Depot depot, int duration,
			long size) throws IBPException
	{
		return (allocate(socket_to_read, depot, duration, size, HARD, CIRQ));
	}

	public static final int store(Socket socket, Allocation allocation, byte[] buffer_out,
			long count_bytes_to_write) throws IBPException
	{
		String ibp_server = allocation != null ? "IBP [" + allocation.depot + "]" : "IBP [unknown]";
		TransferThread transfer_thread = (TransferThread) Thread.currentThread();
		int timeout = Configuration.dlt_depot_request_timeout;

		try {
			socket.setSoTimeout(timeout);

			OutputStream stream_out = socket.getOutputStream();
			// * InputStream is=s.getInputStream();
			BufferedReader buffer_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String ibp_command = new String(PROTOCOL_VERSION + " " + STORE + " "
					+ allocation.capability_write.key + " " + allocation.capability_write.key_wrm + " "
					+ count_bytes_to_write + " " + (timeout / 1000) + "\n");

			log.info(ibp_server + ": outgoing command [" + ibp_command + "]");
			transfer_thread.transfer_status = ibp_server + ": requesting storage ["
					+ count_bytes_to_write + " bytes]";

			stream_out.write(ibp_command.getBytes());

			StringBuffer ibp_response = new StringBuffer();
			// int data=is.read();
			// while((char)data!='\n') {
			// result.append((char)data);
			// data=is.read();
			// }
			ibp_response.append(buffer_in.readLine());

			log.info(ibp_server + ": response [" + ibp_response + "]");
			transfer_thread.transfer_status = ibp_server + ": response [" + ibp_response + "]";

			int ibp_response_code = Integer.parseInt(ibp_response.toString().trim());
			if (ibp_response_code != SUCCESS) {
				throw (new IBPException(ibp_response_code));
			}

			log.info(ibp_server + ": writing [" + count_bytes_to_write + " bytes]");
			transfer_thread.transfer_status = ibp_server + ": writing [" + count_bytes_to_write
					+ " bytes]";

			stream_out.write(buffer_out);

			ibp_response = new StringBuffer();
			// data=is.read();
			// while(data!=-1) {
			// result.append((char)data);
			// data=is.read();
			// }
			ibp_response.append(buffer_in.readLine());

			log.info(ibp_server + ": response [" + ibp_response + "]");
			transfer_thread.transfer_status = ibp_server + ": response [" + ibp_response + "]";

			String[] ibp_response_fields = ibp_response.toString().split(" ");
			ibp_response_code = Integer.parseInt(ibp_response_fields[0].trim());
			if (ibp_response_code != SUCCESS) {
				throw (new IBPException(ibp_response_code));
			}

			int count_bytes_written = Integer.parseInt(ibp_response_fields[1].trim());

			log.info(ibp_server + ": wrote [" + count_bytes_written + " bytes]");
			transfer_thread.transfer_status = ibp_server + ": wrote [" + count_bytes_written + " bytes]";

			return (count_bytes_written);
		} catch (IOException e) {
			throw (new IBPException(-1));
		}
	}

	public static final int load(Socket socket, Allocation allocation, byte[] buffer_out, long size,
			long readOffset, int offset_write) throws IBPException
	{
		String ibp_server = allocation != null ? "IBP [" + allocation.depot + "]" : "IBP [unknown]";
		TransferThread transfer_thread = (TransferThread) Thread.currentThread();
		int timeout = Configuration.dlt_depot_request_timeout;

		try {
			String ibp_command = PROTOCOL_VERSION + " " + LOAD + " " + allocation.capability_read.key
					+ " " + allocation.capability_read.key_wrm + " " + readOffset + " " + size + " "
					+ (timeout/1000) + "\n";

			log.info(ibp_server + ": outgoing command [" + ibp_command + "]");
			transfer_thread.transfer_status = ibp_server + ": requesting [" + readOffset + " - "
					+ (readOffset + size - 1) + "] (" + size + " bytes)";

			socket.setSoTimeout(timeout);
			OutputStream stream_out = socket.getOutputStream();
			stream_out.write(ibp_command.getBytes());

			InputStream stream_in = socket.getInputStream();
			StringBuffer ibp_response = new StringBuffer();
			int data = stream_in.read();
			while ((char) data != '\n') {
				ibp_response.append((char) data);
				data = stream_in.read();
			}

			log.info(ibp_server + ": response [" + ibp_response + "]");
			transfer_thread.transfer_status = ibp_server + ": response [" + ibp_response + "]";

			int ibp_response_code;
			int count_bytes_response;
			if (ibp_response.toString().indexOf('-') != -1) {
				ibp_response_code = Integer.parseInt(ibp_response.toString().trim());
				throw (new IBPException(ibp_response_code));
			} else {
				String[] fields = ibp_response.toString().split(" ");

				ibp_response_code = Integer.parseInt(fields[0]);
				count_bytes_response = Integer.parseInt(fields[1]);
			}

			log.info(ibp_server + ": reading [" + readOffset + " - "
					+ (readOffset + count_bytes_response - 1) + "] (" + count_bytes_response + " bytes)");
			transfer_thread.transfer_status = ibp_server + ": reading [" + readOffset + " - "
					+ (readOffset + count_bytes_response - 1) + "] (" + count_bytes_response + " bytes)";

			int count_bytes_read = stream_in.read(buffer_out, offset_write, count_bytes_response);
			while (count_bytes_read < count_bytes_response) {
//				if (!socket.isClosed()) {
					count_bytes_read += stream_in.read(buffer_out, offset_write + count_bytes_read,
							count_bytes_response - count_bytes_read);
//				} else {
//					log.info(ibp_server + ": failed to read [" + readOffset + " - "
//							+ (readOffset + count_bytes_response - 1) + "] (" + count_bytes_response + " bytes)");
//					transfer_thread.transfer_status = ibp_server + ": failed to read [" + readOffset + " - "
//							+ (readOffset + count_bytes_response - 1) + "] (" + count_bytes_response + " bytes)";
//					break;
//				}
			}

			log.info(ibp_server + ": read [" + readOffset + " - "
					+ (readOffset + count_bytes_response - 1) + "] (" + count_bytes_response + " bytes)");
			transfer_thread.transfer_status = ibp_server + ": read [" + readOffset + " - "
					+ (readOffset + count_bytes_response - 1) + "] (" + count_bytes_response + " bytes)";

			return (count_bytes_read);
		} catch (IOException e) {
			e.printStackTrace();
			log.severe(ibp_server + ".load(): " + e);
			transfer_thread.transfer_status = ibp_server + ".load(): error occurred.";
			throw (new IBPException(-1));
		}
	}

	public static final int copy(Allocation allocation_src, Allocation allocation_dst,
			long count_bytes_to_copy, long offset_to_copy) throws IBPException
	{
		String ibp_server = allocation_src != null ? "IBP [" + allocation_src.depot + "]"
				: "IBP [unknown]";
		TransferThread transfer_thread = (TransferThread) Thread.currentThread();
		int timeout = Configuration.dlt_depot_request_timeout;

		try {
			Depot depot = allocation_src.depot;

			Socket socket = new Socket(depot.host, depot.port);
			socket.setSoTimeout(timeout);

			OutputStream stream_out = socket.getOutputStream();
			// * InputStream is=s.getInputStream();
			BufferedReader buffer_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String ibp_command = new String(PROTOCOL_VERSION + " " + SEND + " "
					+ allocation_src.capability_read.key + " " + allocation_dst.capability_write + " "
					+ allocation_src.capability_read.key_wrm + " " + offset_to_copy + " "
					+ count_bytes_to_copy + " " + (timeout / 1000) + " " + (timeout / 1000) + " "
					+ (timeout / 1000) + "\n");

			log.info(ibp_server + ": outgoing command [" + ibp_command + "]");
			transfer_thread.transfer_status = ibp_server + ": requesting copy [" + offset_to_copy + " - "
					+ (offset_to_copy + count_bytes_to_copy - 1) + "] (" + count_bytes_to_copy + ") ["
					+ allocation_src.depot + " -> " + allocation_dst.depot + "]";

			stream_out.write(ibp_command.getBytes());

			StringBuffer ibp_response = new StringBuffer();
			// int data=is.read();
			// while((char)data!='\n') {
			// tmp.append((char)data);
			// data=is.read();
			// }
			ibp_response.append(buffer_in.readLine());
			socket.close();

			log.info(ibp_server + ": response [" + ibp_response + "]");
			transfer_thread.transfer_status = ibp_server + ": response [" + ibp_response + "]";

			int ibp_response_code;
			int count_bytes_copied = 0;

			if (ibp_response.toString().indexOf('-') != -1) {
				ibp_response_code = Integer.parseInt(ibp_response.toString().trim());
				throw (new IBPException(ibp_response_code));
			} else {
				String[] ibp_response_fields = ibp_response.toString().split(" ");

				ibp_response_code = Integer.parseInt(ibp_response_fields[0]);
				count_bytes_copied = Integer.parseInt(ibp_response_fields[1]);
			}

			log.info(ibp_server + ": copied [" + offset_to_copy + " - "
					+ (offset_to_copy + count_bytes_to_copy - 1) + "] (" + count_bytes_to_copy + " bytes)");
			transfer_thread.transfer_status = ibp_server + ": copied [" + offset_to_copy + " - "
					+ (offset_to_copy + count_bytes_to_copy - 1) + "] (" + count_bytes_to_copy + " bytes)";

			return (count_bytes_copied);
		} catch (IOException e) {
			log.severe(ibp_server + ".copy(): IOException: " + e);
			throw (new IBPException(-1));
		}
	}
}
