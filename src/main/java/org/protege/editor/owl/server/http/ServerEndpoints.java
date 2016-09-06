package org.protege.editor.owl.server.http;

/**
 * Represents the service end-points provided by the HTTP server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ServerEndpoints {

	public static final String ROOT_PATH = "/nci_protege";

	public static final String LOGIN = ROOT_PATH + "/login";

	public static final String PROJECT = ROOT_PATH + "/meta/project";
	public static final String PROJECT_SNAPSHOT = ROOT_PATH + "/meta/project/snapshot";
	public static final String PROJECTS = ROOT_PATH + "/meta/projects";
	public static final String METAPROJECT = ROOT_PATH + "/meta/metaproject";

	public static final String ALL_CHANGES = ROOT_PATH + "/all_changes"; 
	public static final String LATEST_CHANGES = ROOT_PATH + "/latest_changes"; 
	public static final String HEAD = ROOT_PATH + "/head";
	public static final String COMMIT = ROOT_PATH + "/commit";

	public static final String GEN_CODE = ROOT_PATH + "/gen_code";
	public static final String GEN_CODES = ROOT_PATH + "/gen_codes";
	public static final String EVS_REC = ROOT_PATH + "/evs_record";

	public static final String SERVER_RESTART = ROOT_PATH + "/server/restart";
	public static final String SERVER_STOP = ROOT_PATH + "/server/stop";
}
