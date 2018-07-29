/*******************************************************************************
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package smile.symbolic.internal;

/**
 * @author Ernest DeFoy
 */
public class Negation extends UnaryNode {

    public Negation(Expression exp) {

        super(exp, UnaryOperator.NEGATE);
    }

    @Override
    public Expression derive() {

        if(exp instanceof Cosine)
            return exp.derive();

        return new Negation(exp.derive());
    }

    @Override
    public Expression reduce() {

        Expression e = exp.reduce();

        if(e instanceof Negation) {
            return e.getRightChild();
        }

        return new Negation(e);
    }

    @Override
    public double getValue() {
        return 0;
    }
}
