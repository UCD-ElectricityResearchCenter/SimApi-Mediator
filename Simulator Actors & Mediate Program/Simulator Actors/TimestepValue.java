/* Issue the value of Timestep.

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SQLStatement

/**
 Issue the value of Timestep for other actors
 The output is int description of the result.

 @author Fangyijie Wang
 @version $Id: TimestepValue.java 57044 2014-08-26 11:41:05Z cxh $
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class TimestepValue extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimestepValue(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);

        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.INT);

        // Constrain the output type to be a string

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The public output port.
     */
    public TypedIOPort output;

    /** The public input port, which is a multiport.
     */
    public TypedIOPort input;

    /** The private parameter timestepValue used to
     *  get the value of Timestep;
     */
    private int _timestepValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Initialize this TimestepValue.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the input or output parameters are incorrect, or
     *   if there is no effigy for the top level container, or if a problem
     *   occurs creating the effigy and tableau.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _timestepValue = 0;
    }


    /** Perform the query on the database and produce the result
     *  on the output port.
     *  @exception IllegalActionException If the database query fails.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        int width = input.getWidth();

        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);

                double value = 0.0;
                double valueWithoutFormat;

                if (token instanceof DoubleToken) {
                    value = ((DoubleToken) token).doubleValue();
                }

                valueWithoutFormat = value/900.0 + 1.0;

                _timestepValue = (int)valueWithoutFormat;

                output.send(0, new IntToken(_timestepValue));
            }
        }
    }
}
