package com.normbuild.regulation.service;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SeedRegulationCatalog {

    public List<SeedRegulationDocument> bogotaDocuments() {
        return List.of(
                SeedRegulationDocument.from(
                        "Plan de Ordenamiento Territorial de Bogotá",
                        "Bogotá D.C.",
                        "Decreto Distrital 555 de 2021",
                        "Licenciamiento y aplicación de norma urbana",
                        """
                        Para un proyecto residencial en suelo urbano de Bogotá, la revisión normativa debe iniciar con la identificación del tratamiento urbanístico, área de actividad, edificabilidad permitida y condiciones del predio. La licencia urbanística exige verificar que el uso residencial sea compatible con la norma aplicable al sector, que el área construida propuesta respete las condiciones urbanísticas vigentes y que el diseño soporte la ocupación, altura, volumetría y obligaciones urbanísticas que correspondan.
                        """
                ),
                SeedRegulationDocument.from(
                        "Plan de Ordenamiento Territorial de Bogotá",
                        "Bogotá D.C.",
                        "Decreto Distrital 555 de 2021",
                        "Edificabilidad, ocupación y volumetría",
                        """
                        La evaluación de edificabilidad para vivienda debe contrastar el área del lote, el área construida propuesta, el índice de ocupación, el índice de construcción, la altura máxima permitida y la volumetría autorizada por la norma urbana del sector. Cuando el proyecto proponga dos pisos y una altura total cercana a 7.5 metros, se debe validar que la altura sea compatible con el tratamiento urbanístico, las condiciones de frente, aislamiento, antejardín y demás reglas específicas del área normativa.
                        """
                ),
                SeedRegulationDocument.from(
                        "Plan de Ordenamiento Territorial de Bogotá",
                        "Bogotá D.C.",
                        "Decreto Distrital 555 de 2021",
                        "Aislamientos, retiros y condiciones del predio",
                        """
                        Los retiros, aislamientos posteriores y laterales, antejardines y empates volumétricos deben revisarse antes de radicar una licencia de construcción. En proyectos residenciales bifamiliares, el cumplimiento depende de la localización del predio, el tratamiento urbanístico, el tipo de edificación, la altura propuesta y las condiciones de colindancia. Si la norma específica exige aislamiento posterior o lateral, el plano arquitectónico debe demostrar la distancia libre y su continuidad.
                        """
                ),
                SeedRegulationDocument.from(
                        "Reglamento Colombiano de Construcción Sismo Resistente",
                        "Bogotá D.C.",
                        "NSR-10",
                        "Revisión estructural para edificaciones de vivienda",
                        """
                        Toda edificación nueva destinada a vivienda debe cumplir los requisitos del Reglamento Colombiano de Construcción Sismo Resistente. Para una vivienda de dos pisos, el solicitante debe contar con diseño estructural, memoria de cálculo, planos estructurales y revisión de cargas, cimentación y elementos de resistencia sísmica. El cumplimiento urbano no reemplaza la obligación de acreditar seguridad estructural dentro del trámite de licencia.
                        """
                ),
                SeedRegulationDocument.from(
                        "Régimen nacional de licencias urbanísticas",
                        "Bogotá D.C.",
                        "Decreto Único Reglamentario 1077 de 2015",
                        "Licencia de construcción y documentos técnicos",
                        """
                        La licencia de construcción autoriza desarrollar obras de edificación cuando el proyecto acredita cumplimiento urbanístico, arquitectónico y técnico. Para vivienda residencial se deben presentar planos arquitectónicos, estudios y diseños técnicos, identificación del predio, titularidad o autorización correspondiente y documentación exigida por la autoridad competente. La autoridad revisa uso, aprovechamiento, volumetría, accesibilidad, seguridad y compatibilidad normativa.
                        """
                )
        );
    }
}
