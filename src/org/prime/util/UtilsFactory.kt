package org.prime.util

import org.prime.util.machines.LinearBoundedAutomaton
import org.prime.util.machines.TapeMachine
import org.prime.util.machines.TapeMachineType
import org.prime.util.machines.TuringMachine

object UtilsFactory {
    fun getConverter(type: TapeMachineType): Converter =
         when(type) {
            TapeMachineType.TuringMachine -> ConverterT0()
            TapeMachineType.LinearBoundedAutomaton -> ConverterT1()
        }

    fun getDerivationBuilder(type: TapeMachineType, machine: TapeMachine): DerivationBuilder =
        when(type) {
            TapeMachineType.TuringMachine -> DerivationBuilderT0(machine as TuringMachine)
            TapeMachineType.LinearBoundedAutomaton -> DerivationBuilderT1(machine as LinearBoundedAutomaton)
        }
}