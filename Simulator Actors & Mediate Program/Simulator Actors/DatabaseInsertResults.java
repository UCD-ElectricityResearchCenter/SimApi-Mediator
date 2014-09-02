/* Insert input arrays of records into a table.

 Copyright (c) 2014 The Regents of the University College Dublin.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY COLLEGE DUBLIN BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY COLLEGE DUBLIN HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY COLLEGE DUBLIN SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY COLLEGE DUBLIN
 HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package myActors;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Sink;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DatabaseInsertResults

/**
 Insert the input arrays of records into the specified table.
 The table needs to exist in the database and have columns with
 the same names as the record field names. This actor optionally
 clears the table in its initialize() method. If no errors occur
 during insertion, then it commits the changes in its wrapup()
 method.

 @author Fangyijie Wang
 @version $Id: DatabaseInsertResults.java 57044 2014-07-23 16:25:05Z cxh $
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DatabaseInsertResults extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DatabaseInsertResults(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        databaseManager = new StringParameter(this, "databaseManager");
        databaseManager.setExpression("DatabaseManager");

        table = new StringParameter(this, "table");
        table.setExpression("Sensor");

        clear = new Parameter(this, "clear");
        clear.setExpression("false");
        clear.setTypeEquals(BaseType.BOOLEAN);

        // Constrain the output type to be a record type with
        // unspecified fields.
        // NOTE: The output is actually a subtype of this.
        // This is OK because lossless conversion occurs at the
        // output, which (as of 6/19/08) leaves the record unchanged.
        input.setTypeEquals(BaseType.DOUBLE_MATRIX);

        timestepInput = new TypedIOPort(this, "timestepInput", true, false);
        timestepInput.setMultiport(true);
        timestepInput.setTypeEquals(BaseType.INT);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If true, clear the table at initialization of the model.
     *  This is a boolean that defaults to false.
     */
    public Parameter clear;

    /** Name of the DatabaseManager to use.
     *  This defaults to "DatabaseManager".
     */
    public StringParameter databaseManager;

    /** The public input port, which is a multiport.
     */
    public TypedIOPort timestepInput;

    /** Name of the table to set.
     *  This defaults to "v_people".
     */
    public StringParameter table;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the table to contain all the rows in input array of records.
     *  @exception IllegalActionException If the database update fails.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        int width = timestepInput.getWidth();


        if (input.hasToken(0)) {

                String databaseName = databaseManager.stringValue();
                DatabaseManager database = DatabaseManager.findDatabaseManager(
                        databaseName, this);

                DoubleMatrixToken arrTok = (DoubleMatrixToken) (input.get(0));

                int timestepValue=0;

                for (int i = 0; i < width; i++) {
                    if (timestepInput.hasToken(i)) {
                        Token token = timestepInput.get(i);
                        timestepValue = ((IntToken) token).intValue();
                    }
                }

                int n = arrTok.getRowCount();
                double[] ret = new double[n];
                for (int i = 0; i < n; i++) {
                	DoubleToken scaTok = (DoubleToken) arrTok.getElementAsToken(
                            i, 0);
                    ret[i] = scaTok.doubleValue();
                    if (Double.isNaN(ret[i])) {
                        final String em = "Actor " + this.getFullName() + ": "
                                + "Token number " + i + " is NaN at time "
                                + getDirector().getModelTime().getDoubleValue();
                        throw new IllegalActionException(this, em);
                    }
                }

                String getInstanceId = "select idInstance from Instance where begin = 1";
                Double instanceIdDou = database.executeDouble(getInstanceId);
                int instanceId = instanceIdDou.intValue();

                String prefix = "insert into Sensor " +
                		" ( idSensor, name, room, Timestep_idTimestep, Timestep_Instance_idInstance, Environment, Kitchen, Living, Corridor, " +
                		"Study, Bath1, Bedroom1, Bedroom2, Bedroom3, Bedroom4, Bath2, StorageTankHeating1, " +
                		"StorageTankHeating2, EMS_PVProductionEMS, EMS_BuildingConsumption ) " +
                        "values ( " + "1, 'unknow', null, " + timestepValue + ", " + instanceId + ", " + ret[0] + ", "
								    + ret[1] + ", " + ret[2] + ", " + ret[3] + ", " + ret[4] + ", " + ret[5] + ", " + ret[6] + ", "
						    	    + ret[7] + ", " + ret[8] + ", " + ret[9] + ", " + ret[10] + ", " + ret[11] + ", " + ret[12] + ", "
		                            + ret[13] + ", " + ret[14] + " )";
                String sql = prefix;

                if (_debugging) {
                    _debug("Issuing statement:\n" + sql);
                }
                database.execute(sql);
        }
    }

    /** Clear the specified table if the <i>clear</i> parameter is true.
     *  @exception IllegalActionException If the database query fails.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (((BooleanToken) clear.getToken()).booleanValue()) {
            String databaseName = databaseManager.stringValue();
            DatabaseManager database = DatabaseManager.findDatabaseManager(
                    databaseName, this);
            String query = "delete from " + table.stringValue() + ";";
            if (_debugging) {
                _debug("Issuing statement:\n" + query);
            }
            // It would be nice to have an option to not commit the
            // transaction until wrapup, but, sadly, this doens't
            // work, at least not with MySQL. So we have to
            // commit each time.
            database.execute(query);
        }
    }
}
