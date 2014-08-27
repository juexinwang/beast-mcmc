package dr.app.beauti.components.linkedparameters;

import dr.app.beauti.options.*;
import dr.app.beauti.priorsPanel.PriorsPanel;
import dr.app.beauti.types.PriorScaleType;
import dr.app.beauti.types.PriorType;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Marc A. Suchard
 * @version $Id$
 */
public class LinkedParameterComponentOptions implements ComponentOptions {

    public LinkedParameterComponentOptions(final BeautiOptions options) {
        this.options = options;
        linkedParameterMap = new HashMap<Parameter, LinkedParameter>();
        argumentParameterMap = new HashMap<Parameter, LinkedParameter>();
    }

    public void createParameters(final ModelOptions modelOptions) {
        // Do nothing; this is only called at launch
    }

    public void selectParameters(final ModelOptions modelOptions, final List<Parameter> params) {
        for (LinkedParameter linkedParameter : getLinkedParameterList()) {
            params.addAll(linkedParameter.getArgumentParameterList());
        }
    }

    public void selectStatistics(final ModelOptions modelOptions, final List<Parameter> stats) {
        // No statistics
    }

    public void selectOperators(final ModelOptions modelOptions, final List<Operator> ops) {
        for (LinkedParameter linkedParameter : getLinkedParameterList()) {
            for (Parameter parameter : linkedParameter.getArgumentParameterList()) {
            }
        }
    }

    public boolean nameExits(String name) {
        boolean found = false;

        if (options.parameterExists(name)) {
            return true;
        }

        for (LinkedParameter linkedParameter : getLinkedParameterList()) {
            if (linkedParameter.getName().equalsIgnoreCase(name)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public LinkedParameter createLinkedParameter(String name, List<Parameter> parameterList) {
        List<Parameter> argumentList = new ArrayList<Parameter>();

        Parameter sourceParameter = parameterList.get(0);
        Parameter newParameter = options.createDuplicate(name, "Linked parameter", sourceParameter);

        options.createScaleOperator(name, options.demoTuning, 1.0);

        argumentList.add(newParameter);

        LinkedParameter linkedParameter = new LinkedParameter(name, argumentList);

        return linkedParameter;
    }



    public LinkedParameter addLinkedParameter(LinkedParameter linkedParameter, List<Parameter> parameterList) {
        for (Parameter parameter : parameterList) {
            linkedParameterMap.put(parameter, linkedParameter);
            parameter.isLinked = true;
            parameter.linkedName = linkedParameter.getName();
        }

        for (Parameter parameter : linkedParameter.getArgumentParameterList()) {
            argumentParameterMap.put(parameter, linkedParameter);
        }

        return linkedParameter;
    }

    public void removeParameters(PriorsPanel priorsPanel, List<Parameter> parametersToRemove, boolean caution) {
        for (Parameter parameter : parametersToRemove) {
            linkedParameterMap.remove(parameter);
            parameter.isLinked = false;
        }
    }

    public Collection<LinkedParameter> getLinkedParameterList() {
        return new LinkedHashSet<LinkedParameter>(linkedParameterMap.values());
    }

    public LinkedParameter getLinkedParameter(Parameter parameter) {
        return linkedParameterMap.get(parameter);
    }

    public LinkedParameter getLinkedParameterForArgument(Parameter parameter) {
        return argumentParameterMap.get(parameter);
    }

    public List<Parameter> getParameters(LinkedParameter linkedParameter) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        for (Parameter parameter : linkedParameterMap.keySet()) {
            if (linkedParameterMap.get(parameter) == linkedParameter) {
                parameters.add(parameter);
            }
        }
        return parameters;
    }



    final private BeautiOptions options;
    final private Map<Parameter, LinkedParameter> linkedParameterMap;
    final private Map<Parameter, LinkedParameter> argumentParameterMap;

//    public void generatePriors(final XMLWriter writer) {
//        for (HierarchicalPhylogeneticModel hpm : hpmList) {
//            hpm.generatePriors(writer);
//        }
//    }

    public boolean isEmpty() {
        return linkedParameterMap.isEmpty();
    }
}