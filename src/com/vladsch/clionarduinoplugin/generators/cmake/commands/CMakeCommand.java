package com.vladsch.clionarduinoplugin.generators.cmake.commands;

import com.vladsch.clionarduinoplugin.generators.cmake.CMakeListsBuilder;
import com.vladsch.clionarduinoplugin.generators.cmake.CMakeParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class CMakeCommand implements CMakeElement {
    final protected @NotNull CMakeCommandType myCommandType;
    final protected @NotNull ArrayList<String> myArgs;
    protected boolean myAddEOL;

    public CMakeCommand(@NotNull final CMakeCommandType commandType, @NotNull final ArrayList<String> args, boolean isAddEOL) {
        myCommandType = commandType;
        myArgs = args;
        myAddEOL = isAddEOL;

        String[] defaults = commandType.getDefaultArgs();
        int iMax = defaults.length;

        // set defaults
        for (int i = myArgs.size(); i < iMax; i++) {
            addArg(defaults[i]);
        }
    }

    public CMakeCommand(@NotNull final CMakeCommandType commandType, @NotNull final ArrayList<String> args) {
        this(commandType, args, true);
    }

    public CMakeCommand(@NotNull final CMakeCommandType commandType, boolean isAddEOL) {
        this(commandType, new ArrayList<>(), isAddEOL);
    }

    public CMakeCommand(@NotNull final CMakeCommandType commandType) {
        this(commandType, new ArrayList<>(), true);
    }

    @Override
    public @NotNull String getText(@Nullable Map<String, Object> valueSet) {
        StringBuilder sb = new StringBuilder();
        try {
            appendTo(sb, valueSet);
        } catch (IOException ignored) {

        }
        return sb.toString();
    }

    @Override
    public boolean isAddEOL() {
        return myAddEOL;
    }

    @Override
    public void setAddEOL(final boolean addEOL) {
        myAddEOL = addEOL;
    }

    @Override
    public void appendTo(StringBuilder out, @Nullable Map<String, Object> valueSet) throws IOException {
        out.append(myCommandType.getCommand());
        out.append("(");
        String sep = "";

        HashSet<String> argValues = new HashSet<>();

        for (String arg : myCommandType.getFixedArgs()) {
            if (!myCommandType.isNoDupeArgs() || !argValues.contains(arg)) {
                argValues.add(arg);

                out.append(sep);
                sep = " ";

                out.append(CMakeParser.getArgText(CMakeListsBuilder.replacedCommandParams(arg, valueSet)));
            }
        }

        for (String arg : myArgs) {
            if (!myCommandType.isNoDupeArgs() || !argValues.contains(arg)) {
                argValues.add(arg);

                out.append(sep);
                sep = " ";
                out.append(CMakeParser.getArgText(arg));
            }
        }

        if (myAddEOL) {
            out.append(")\n");
        } else {
            out.append(")");
        }
    }

    @NotNull
    public CMakeCommandType getCommandType() {
        return myCommandType;
    }

    public List<String> getArgs() {
        return myArgs;
    }

    public int getArgCount() {
        return myArgs.size();
    }

    public void setArgs(@NotNull Collection<String> args) {
        myArgs.clear();
        myArgs.addAll(args);
    }

    public void setArg(int index, @NotNull String arg) {
        if (index == getArgCount()) {
            myArgs.add(arg);
        } else {
            myArgs.set(index, arg);
        }
    }

    public @NotNull String getArg(int index) {
        return myArgs.get(index);
    }

    public void addArg(@NotNull String arg) {
        myArgs.add(arg);
    }

    public void addArg(int index, @NotNull String arg) {
        myArgs.set(index, arg);
    }

    public void removeArg(int index) {
        myArgs.remove(index);
    }

    @Override
    public String toString() {
        return "CMakeCommand{" +
                "" + myCommandType +
                ", =" + myArgs +
                '}';
    }
}